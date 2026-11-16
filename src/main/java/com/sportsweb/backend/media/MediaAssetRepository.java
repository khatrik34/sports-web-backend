package com.sportsweb.backend.media;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface MediaAssetRepository extends JpaRepository<MediaAsset, UUID>, JpaSpecificationExecutor<MediaAsset> {
}
