package com.aruba.zeta.usermgmt.entity;

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
 * JPA entity representing a user profile stored in the {@code users} table.
 */
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class UserEntity {

    /** Auto-generated UUID primary key. */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** Unique login name for the user. */
    @Column(nullable = false, unique = true)
    private String username;

    /** Unique email address of the user. */
    @Column(nullable = false, unique = true)
    private String email;

    /** First name of the user. */
    @Column(name = "first_name", nullable = false)
    private String firstName;

    /** Last name of the user. */
    @Column(name = "last_name", nullable = false)
    private String lastName;

    /** Indicates whether the user account is active. */
    @Column(name = "active", nullable = false)
    @Builder.Default
    private boolean isActive = true;

    // TODO replace with actual enum for Role Based Access control
    @Column(nullable = false)
    @Builder.Default
    private String role = "ROLE_USER";

    /**
     * Flag to enable semantic indexing of user documents
     */
    @Column(name = "semantic_indexing_enabled", nullable = false)
    @Builder.Default
    private boolean semanticIndexingEnabled = false;

    /** Timestamp when the user record was created. */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /** Timestamp when the user record was last updated. */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
