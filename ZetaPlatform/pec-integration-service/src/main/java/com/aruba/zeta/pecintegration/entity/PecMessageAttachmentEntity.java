package com.aruba.zeta.pecintegration.entity;

import jakarta.persistence.*;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Entity
@Table(name = "message_attachment_refs", indexes = {
    @Index(name = "idx_attachment_message_id", columnList = "message_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PecMessageAttachmentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "message_id", nullable = false)
    private PecMessageEntity message;

    // References the Object ID or Document ID in the DOCs Management Service (Ceph)
    @Column(name = "document_id", nullable = false, updatable = false)
    private UUID documentId;
    
    @Column(name = "file_name", length = 255)
    private String fileName;
}
