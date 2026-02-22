package com.aruba.zeta.pecintegration.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aruba.zeta.pecintegration.entity.PecMessageEntity;

public interface PecMessageRepo extends JpaRepository<PecMessageEntity, UUID> {
    
}
