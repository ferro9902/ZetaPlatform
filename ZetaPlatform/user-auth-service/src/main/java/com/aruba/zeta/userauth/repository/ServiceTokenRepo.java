package com.aruba.zeta.userauth.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aruba.zeta.userauth.entity.ServiceTokenEntity;
import com.aruba.zeta.userauth.enums.IntegrationServiceType;

/**
 * Spring Data JPA repository for {@link com.aruba.zeta.userauth.entity.ServiceTokenEntity}.
 * Provides lookup by user and service type, and bulk deletion by user UUID.
 */
public interface ServiceTokenRepo extends JpaRepository<ServiceTokenEntity, UUID> {

    Optional<ServiceTokenEntity> findByUserIdAndServiceType(UUID userId, IntegrationServiceType serviceType);

    void deleteAllByUserId(UUID userId);
}
