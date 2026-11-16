package com.sportsweb.backend.homepage;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface HomepageSectionRepository extends JpaRepository<HomepageSection, UUID> {

    List<HomepageSection> findAllByOrderBySortOrderAsc();

    List<HomepageSection> findByEnabledTrueOrderBySortOrderAsc();
}
