package com.aruba.zeta.userauth.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aruba.zeta.userauth.entity.AuthUserEntity;

/**
 * Spring Data JPA repository for {@link com.aruba.zeta.userauth.entity.AuthUserEntity}.
 * Provides lookup by username and by the user-mgmt-service UUID.
 */
public interface AuthUserRepo extends JpaRepository<AuthUserEntity, UUID> {

    Optional<AuthUserEntity> findByUsername(String username);

    Optional<AuthUserEntity> findByUserId(UUID userId);
}
