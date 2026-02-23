package com.aruba.zeta.pecintegration.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.hibernate.annotations.CreationTimestamp;

import com.aruba.zeta.pecintegration.enums.EMessageStatus;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "pec_messages", indexes = {
    @Index(name = "idx_pec_message_mailbox_id", columnList = "mailbox_id"),
    @Index(name = "idx_pec_message_timestamp", columnList = "message_timestamp"),
    @Index(name = "idx_pec_message_external_id", columnList = "external_message_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PecMessageEntity {@Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mailbox_id", nullable = false)
    private PecMailboxEntity mailbox;

    // The unique identifier returned by the external Aruba PEC systems
    @Column(name = "external_message_id", length = 255)
    private String externalMessageId;

    @Column(name = "sender_address", nullable = false, length = 255)
    private String sender;

    @Column(name = "recipient_address", nullable = false, length = 255)
    private String recipient;

    @Column(name = "subject", length = 1000)
    private String subject;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private EMessageStatus status;

    // The actual time the message was sent/received
    @Column(name = "message_timestamp", nullable = false)
    private Instant messageTimestamp;

    // Time the metadata record was created in this DB
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
