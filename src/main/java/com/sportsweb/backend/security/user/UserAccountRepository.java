package com.sportsweb.backend.security.user;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UserAccountRepository extends JpaRepository<UserAccount, UUID>, JpaSpecificationExecutor<UserAccount> {

    boolean existsByEmailIgnoreCase(String email);

    /** Authentication needs roles and their permissions to build token authorities. */
    @EntityGraph(attributePaths = {"roles", "roles.permissions"})
    @Query("select u from UserAccount u where lower(u.email) = lower(:email)")
    Optional<UserAccount> findForAuthenticationByEmail(@Param("email") String email);

    @EntityGraph(attributePaths = {"roles", "roles.permissions"})
    @Query("select u from UserAccount u where u.id = :id")
    Optional<UserAccount> findForAuthenticationById(@Param("id") UUID id);

    @Query("select count(distinct u) from UserAccount u join u.roles r where r.code = :roleCode and u.enabled = true")
    long countEnabledUsersWithRole(@Param("roleCode") String roleCode);
}
