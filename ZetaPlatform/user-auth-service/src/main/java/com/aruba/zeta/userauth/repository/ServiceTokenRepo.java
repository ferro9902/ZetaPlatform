package com.aruba.zeta.userauth.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aruba.zeta.userauth.entity.ServiceTokenEntity;

public interface ServiceTokenRepo extends JpaRepository<ServiceTokenEntity, UUID> {
    
}
