package com.sportsweb.backend.seo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface RedirectRepository extends JpaRepository<Redirect, UUID>, JpaSpecificationExecutor<Redirect> {

    Optional<Redirect> findByFromPath(String fromPath);

    boolean existsByFromPath(String fromPath);

    boolean existsByFromPathAndIdNot(String fromPath, UUID id);
}
