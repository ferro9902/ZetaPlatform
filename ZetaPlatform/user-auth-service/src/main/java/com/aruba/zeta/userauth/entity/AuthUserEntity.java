package com.aruba.zeta.userauth.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA entity representing authentication credentials stored in the {@code auth_users} table.
 */
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "auth_users")
public class AuthUserEntity {

    /** Auto-generated UUID primary key. */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** Reference to the corresponding user UUID in user-mgmt-service. */
    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    /** Unique login name, mirrored from user-mgmt-service. */
    @Column(nullable = false, unique = true)
    private String username;

    /** Bcrypt hash of the user's password. */
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    /** Timestamp when the auth record was created. */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /** Timestamp when the auth record was last updated. */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
