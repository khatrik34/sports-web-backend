package com.sportsweb.backend.security.admin;

import com.sportsweb.backend.common.audit.AuditAction;
import com.sportsweb.backend.common.audit.AuditService;
import com.sportsweb.backend.common.error.ApiException;
import com.sportsweb.backend.common.web.PageResponse;
import com.sportsweb.backend.common.web.Pageables;
import com.sportsweb.backend.security.CurrentUserProvider;
import com.sportsweb.backend.security.admin.dto.CreateUserRequest;
import com.sportsweb.backend.security.admin.dto.PermissionResponse;
import com.sportsweb.backend.security.admin.dto.RoleResponse;
import com.sportsweb.backend.security.admin.dto.SetPasswordRequest;
import com.sportsweb.backend.security.admin.dto.UpdateUserRequest;
import com.sportsweb.backend.security.admin.dto.UpdateUserRolesRequest;
import com.sportsweb.backend.security.admin.dto.UserResponse;
import com.sportsweb.backend.security.password.PasswordPolicy;
import com.sportsweb.backend.security.token.RefreshTokenService;
import com.sportsweb.backend.security.user.PermissionRepository;
import com.sportsweb.backend.security.user.Role;
import com.sportsweb.backend.security.user.RoleCodes;
import com.sportsweb.backend.security.user.RoleRepository;
import com.sportsweb.backend.security.user.UserAccount;
import com.sportsweb.backend.security.user.UserAccountRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Staff and role administration. Every mutation is audited and every destructive change is
 * guarded so the system cannot lock itself out (last super administrator, self-disable).
 */
@Service
public class UserAdminService {

    private final UserAccountRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;
    private final RefreshTokenService refreshTokenService;
    private final CurrentUserProvider currentUserProvider;

    public UserAdminService(UserAccountRepository userRepository,
                            RoleRepository roleRepository,
                            PermissionRepository permissionRepository,
                            PasswordEncoder passwordEncoder,
                            AuditService auditService,
                            RefreshTokenService refreshTokenService,
                            CurrentUserProvider currentUserProvider) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditService = auditService;
        this.refreshTokenService = refreshTokenService;
        this.currentUserProvider = currentUserProvider;
    }

    @Transactional(readOnly = true)
    public PageResponse<UserResponse> search(String query, Boolean enabled, int page, int size) {
        Specification<UserAccount> specification = (root, criteriaQuery, cb) -> cb.conjunction();
        if (query != null && !query.isBlank()) {
            String pattern = "%" + query.trim().toLowerCase(Locale.ROOT) + "%";
            specification = specification.and((root, criteriaQuery, cb) -> cb.or(
                    cb.like(cb.lower(root.get("email")), pattern),
                    cb.like(cb.lower(root.get("displayName")), pattern)));
        }
        if (enabled != null) {
            specification = specification.and((root, criteriaQuery, cb) -> cb.equal(root.get("enabled"), enabled));
        }
        Page<UserAccount> result = userRepository.findAll(specification,
                Pageables.of(page, size, Sort.by(Sort.Direction.ASC, "email")));
        Instant now = Instant.now();
        return PageResponse.from(result, user -> UserResponse.from(user, now));
    }

    @Transactional(readOnly = true)
    public UserResponse get(UUID id) {
        return UserResponse.from(loadUser(id), Instant.now());
    }

    @Transactional
    public UserResponse create(CreateUserRequest request) {
        String email = normalizeEmail(request.email());
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw ApiException.conflict("email_taken", "A user with this email already exists.");
        }
        PasswordPolicy.enforce(request.password());

        UserAccount user = new UserAccount();
        user.setEmail(email);
        user.setDisplayName(request.displayName().trim());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setEnabled(true);
        user.setPasswordChangedAt(Instant.now());
        boolean mustChange = request.mustChangePassword() == null || request.mustChangePassword();
        user.setMustChangePassword(mustChange);
        resolveRoles(request.roleCodes()).forEach(user::addRole);

        userRepository.save(user);
        auditService.record(AuditAction.USER_CREATED, "user", user.getId().toString(),
                "Created staff user " + email, null, snapshot(user));
        return UserResponse.from(user, Instant.now());
    }

    @Transactional
    public UserResponse update(UUID id, UpdateUserRequest request) {
        UserAccount user = loadUser(id);
        Map<String, Object> before = snapshot(user);
        UUID actorId = currentUserProvider.requireUserId();
        Instant now = Instant.now();

        if (request.displayName() != null && !request.displayName().isBlank()) {
            user.setDisplayName(request.displayName().trim());
        }
        if (request.enabled() != null && request.enabled() != user.isEnabled()) {
            if (!request.enabled()) {
                if (user.getId().equals(actorId)) {
                    throw ApiException.badRequest("cannot_disable_self", "You cannot disable your own account.");
                }
                if (user.hasRole(RoleCodes.SUPER_ADMIN) && userRepository.countEnabledUsersWithRole(RoleCodes.SUPER_ADMIN) <= 1) {
                    throw ApiException.badRequest("last_super_admin",
                            "At least one enabled super administrator must remain.");
                }
            }
            user.setEnabled(request.enabled());
            if (!request.enabled()) {
                refreshTokenService.revokeAllForUser(user.getId(), "ACCOUNT_DISABLED");
            }
        }

        userRepository.save(user);
        auditService.record(AuditAction.USER_UPDATED, "user", user.getId().toString(),
                "Updated staff user " + user.getEmail(), before, snapshot(user));
        return UserResponse.from(user, now);
    }

    @Transactional
    public UserResponse replaceRoles(UUID id, UpdateUserRolesRequest request) {
        UserAccount user = loadUser(id);
        Map<String, Object> before = snapshot(user);
        Set<Role> newRoles = resolveRoles(request.roleCodes());

        boolean wasSuperAdmin = user.hasRole(RoleCodes.SUPER_ADMIN);
        boolean staysSuperAdmin = newRoles.stream().anyMatch(role -> role.getCode().equals(RoleCodes.SUPER_ADMIN));
        if (wasSuperAdmin && !staysSuperAdmin
                && userRepository.countEnabledUsersWithRole(RoleCodes.SUPER_ADMIN) <= 1) {
            throw ApiException.badRequest("last_super_admin",
                    "At least one enabled super administrator must remain.");
        }

        user.clearRoles();
        newRoles.forEach(user::addRole);
        userRepository.save(user);
        auditService.record(AuditAction.USER_ROLES_CHANGED, "user", user.getId().toString(),
                "Changed roles of " + user.getEmail(), before, snapshot(user));
        return UserResponse.from(user, Instant.now());
    }

    @Transactional
    public void resetPassword(UUID id, SetPasswordRequest request) {
        UserAccount user = loadUser(id);
        PasswordPolicy.enforce(request.password());
        boolean mustChange = request.mustChangePassword() == null || request.mustChangePassword();

        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setPasswordChangedAt(Instant.now());
        user.setMustChangePassword(mustChange);
        user.resetFailedLoginAttempts();
        userRepository.save(user);

        refreshTokenService.revokeAllForUser(user.getId(), "PASSWORD_RESET");
        // The audit snapshot deliberately contains no password material.
        auditService.record(AuditAction.USER_PASSWORD_RESET, "user", user.getId().toString(),
                "Password reset for " + user.getEmail(), null, Map.of("mustChangePassword", mustChange));
    }

    @Transactional
    public UserResponse unlock(UUID id) {
        UserAccount user = loadUser(id);
        user.setLockedUntil(null);
        user.resetFailedLoginAttempts();
        userRepository.save(user);
        auditService.record(AuditAction.USER_UNLOCKED, "user", user.getId().toString(),
                "Unlocked account " + user.getEmail(), null, null);
        return UserResponse.from(user, Instant.now());
    }

    @Transactional(readOnly = true)
    public List<RoleResponse> roles() {
        return roleRepository.findAllByOrderByCodeAsc().stream().map(RoleResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<PermissionResponse> permissions() {
        return permissionRepository.findAllByOrderByCodeAsc().stream().map(PermissionResponse::from).toList();
    }

    private UserAccount loadUser(UUID id) {
        return userRepository.findById(id).orElseThrow(() -> ApiException.notFound("User"));
    }

    private Set<Role> resolveRoles(Set<String> roleCodes) {
        List<String> codes = roleCodes.stream()
                .map(code -> code == null ? "" : code.trim().toUpperCase(Locale.ROOT))
                .filter(code -> !code.isEmpty())
                .distinct()
                .toList();
        if (codes.isEmpty()) {
            throw ApiException.badRequest("role_required", "At least one role is required.");
        }
        List<Role> roles = roleRepository.findAllByCodeIn(codes);
        if (roles.size() != codes.size()) {
            Set<String> found = new LinkedHashSet<>(roles.stream().map(Role::getCode).toList());
            List<String> unknown = codes.stream().filter(code -> !found.contains(code)).toList();
            throw ApiException.badRequest("unknown_role", "Unknown role code(s): " + String.join(", ", unknown));
        }
        return new LinkedHashSet<>(roles);
    }

    private Map<String, Object> snapshot(UserAccount user) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("email", user.getEmail());
        snapshot.put("displayName", user.getDisplayName());
        snapshot.put("enabled", user.isEnabled());
        snapshot.put("mustChangePassword", user.isMustChangePassword());
        snapshot.put("roles", user.getRoles().stream().map(Role::getCode).sorted().toList());
        return snapshot;
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }
}
