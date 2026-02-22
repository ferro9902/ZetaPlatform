package com.aruba.zeta.usermgmt.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aruba.zeta.usermgmt.entity.UserEntity;

public interface UserRepo extends JpaRepository<UserEntity, UUID> {
    
}
