package com.aruba.zeta.usermgmt.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aruba.zeta.usermgmt.entity.UserEntity;

/**
 * Spring Data JPA repository for {@link com.aruba.zeta.usermgmt.entity.UserEntity}.
 * Provides standard CRUD operations keyed by UUID.
 */
public interface UserRepo extends JpaRepository<UserEntity, UUID> {
    
}
