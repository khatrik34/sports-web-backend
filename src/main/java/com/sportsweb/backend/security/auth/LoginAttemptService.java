package com.sportsweb.backend.security.auth;

import com.sportsweb.backend.common.audit.AuditAction;
import com.sportsweb.backend.common.audit.AuditService;
import com.sportsweb.backend.config.LoginProperties;
import com.sportsweb.backend.security.user.UserAccount;
import com.sportsweb.backend.security.user.UserAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/** Persists failed/successful sign-in counters and audits them in their own transaction. */
@Service
public class LoginAttemptService {

    private final UserAccountRepository userRepository;
    private final AuditService auditService;
    private final LoginProperties loginProperties;

    public LoginAttemptService(UserAccountRepository userRepository,
                               AuditService auditService,
                               LoginProperties loginProperties) {
        this.userRepository = userRepository;
        this.auditService = auditService;
        this.loginProperties = loginProperties;
    }

    @Transactional
    public void registerFailure(UUID userId) {
        Optional<UserAccount> found = userRepository.findById(userId);
        if (found.isEmpty()) {
            return;
        }
        UserAccount user = found.get();
        Instant now = Instant.now();
        boolean locked = user.registerFailedLogin(
                loginProperties.getMaxFailedAttempts(), loginProperties.getLockoutDuration(), now);
        userRepository.save(user);
        auditService.record(AuditAction.AUTH_LOGIN_FAILED, "user", userId.toString(),
                locked ? "Failed sign-in attempt; account locked" : "Failed sign-in attempt", null, null);
    }

    @Transactional
    public void registerSuccess(UUID userId) {
        Optional<UserAccount> found = userRepository.findById(userId);
        if (found.isEmpty()) {
            return;
        }
        UserAccount user = found.get();
        user.registerSuccessfulLogin(Instant.now());
        userRepository.save(user);
        auditService.record(AuditAction.AUTH_LOGIN_SUCCEEDED, "user", userId.toString(),
                "Successful sign-in", null, null);
    }
}
