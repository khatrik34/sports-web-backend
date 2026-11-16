package com.sportsweb.backend.security.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PermissionRepository extends JpaRepository<Permission, String> {

    List<Permission> findAllByOrderByCodeAsc();
}
