package com.aruba.zeta.userauth.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.aruba.zeta.userauth.enums.IntegrationServiceType;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA entity representing an encrypted OAuth2 token pair for a third-party integration,
 * stored in the {@code service_tokens} table.
 */
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "service_tokens", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "service_type"})
})
public class ServiceTokenEntity {

    /** Auto-generated UUID primary key. */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** Reference to the owning user's UUID. */
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    /** The integration service this token belongs to. */
    @Enumerated(EnumType.STRING)
    @Column(name = "service_type", nullable = false)
    private IntegrationServiceType serviceType;

    /** Access token encrypted at rest using AES-256-GCM. */
    @Column(name = "encrypted_access_token", nullable = false, columnDefinition = "TEXT")
    private String encryptedAccessToken;

    /** Refresh token encrypted at rest using AES-256-GCM. */
    @Column(name = "encrypted_refresh_token", columnDefinition = "TEXT")
    private String encryptedRefreshToken;

    /** Expiration timestamp of the access token (epoch seconds). */
    @Column(name = "token_expires_at", nullable = false)
    private Instant tokenExpiresAt;

    /** Timestamp when the token record was created. */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /** Timestamp when the token record was last updated. */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}