package com.aruba.zeta.pecintegration.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.aruba.zeta.pecintegration.enums.EMailboxStatus;

import java.time.Instant;
import java.util.UUID;


@Entity
@Table(name = "pec_mailboxes", indexes = {
    @Index(name = "idx_pec_mailbox_user_id", columnList = "user_id"),
    @Index(name = "idx_pec_mailbox_address", columnList = "pec_address", unique = true)
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PecMailboxEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // This references to the user-mgmt-service UUID
    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Column(name = "pec_address", nullable = false, unique = true, length = 255)
    private String pecAddress;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private EMailboxStatus status;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
    
}
