package com.sportsweb.backend.security.bootstrap;

import com.sportsweb.backend.common.audit.AuditAction;
import com.sportsweb.backend.common.audit.AuditOutcome;
import com.sportsweb.backend.common.audit.AuditService;
import com.sportsweb.backend.config.BootstrapProperties;
import com.sportsweb.backend.security.password.PasswordPolicy;
import com.sportsweb.backend.security.user.Role;
import com.sportsweb.backend.security.user.RoleCodes;
import com.sportsweb.backend.security.user.RoleRepository;
import com.sportsweb.backend.security.user.UserAccount;
import com.sportsweb.backend.security.user.UserAccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Locale;

/**
 * Creates the first administrator, only when explicitly enabled and only when the user table
 * is empty. Keeps credentials out of migrations and out of logs.
 */
@Service
public class AdminBootstrapService {

    private static final Logger log = LoggerFactory.getLogger(AdminBootstrapService.class);

    private final BootstrapProperties properties;
    private final UserAccountRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    public AdminBootstrapService(BootstrapProperties properties,
                                 UserAccountRepository userRepository,
                                 RoleRepository roleRepository,
                                 PasswordEncoder passwordEncoder,
                                 AuditService auditService) {
        this.properties = properties;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditService = auditService;
    }

    @Transactional
    public void bootstrapIfRequired() {
        if (!properties.isEnabled()) {
            return;
        }

        String email = properties.getAdminEmail() == null ? "" : properties.getAdminEmail().trim().toLowerCase(Locale.ROOT);
        String password = properties.getAdminPassword() == null ? "" : properties.getAdminPassword();

        if (email.isBlank() || password.isBlank()) {
            throw new IllegalStateException(
                    "APP_BOOTSTRAP_ENABLED=true requires APP_BOOTSTRAP_ADMIN_EMAIL and APP_BOOTSTRAP_ADMIN_PASSWORD.");
        }
        if (userRepository.count() > 0) {
            log.info("Bootstrap administrator skipped: staff accounts already exist.");
            return;
        }

        PasswordPolicy.enforce(password);
        Role superAdmin = roleRepository.findByCode(RoleCodes.SUPER_ADMIN)
                .orElseThrow(() -> new IllegalStateException("SUPER_ADMIN role is missing; check Flyway migration V2."));

        UserAccount admin = new UserAccount();
        admin.setEmail(email);
        admin.setDisplayName(properties.getAdminDisplayName() == null || properties.getAdminDisplayName().isBlank()
                ? "Site Administrator"
                : properties.getAdminDisplayName().trim());
        admin.setPasswordHash(passwordEncoder.encode(password));
        admin.setEnabled(true);
        admin.setPasswordChangedAt(Instant.now());
        admin.setMustChangePassword(false);
        admin.addRole(superAdmin);
        userRepository.save(admin);

        auditService.recordSecurityEvent(AuditAction.BOOTSTRAP_ADMIN_CREATED, AuditOutcome.SUCCESS,
                email, admin.getId(), "Bootstrap administrator created from environment configuration");
        log.warn("Bootstrap administrator created for {}. Set APP_BOOTSTRAP_ENABLED=false after the first start.", email);
    }
}
