package com.aruba.zeta.pecintegration.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aruba.zeta.pecintegration.entity.PecMailboxEntity;


public interface PecMailboxRepo extends JpaRepository<PecMailboxEntity, UUID> {
    
}
