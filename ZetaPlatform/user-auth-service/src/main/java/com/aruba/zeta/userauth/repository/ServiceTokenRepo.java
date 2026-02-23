package com.aruba.zeta.userauth.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aruba.zeta.userauth.entity.ServiceTokenEntity;
import com.aruba.zeta.userauth.enums.IntegrationServiceType;

public interface ServiceTokenRepo extends JpaRepository<ServiceTokenEntity, UUID> {

    Optional<ServiceTokenEntity> findByUserIdAndServiceType(UUID userId, IntegrationServiceType serviceType);
}
