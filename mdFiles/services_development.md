# Services Development <img src="../mdImgs/aruba.png" alt="arubapec" width="100" align="right"/>

This document covers the services development for Platform Zeta.

## Developed Microservices

The implementation phase focused on establishing the core infrastructure required for user management, security, and the first vertical integration (PEC).
All of the developed services are located in the (`ZetaPlatform` folder).

The following three components have been developed:

### 1. User Management Service (`user-mgmt-service`)

This service acts as the central registry for all users within the Zeta Platform. It is responsible for the provisioning and lifecycle management of user identities.

* **Core Functions:** Handles User creation, updates, retrieval, and deletion (CRUD) exclusively via a **gRPC** interface (`UserManagementService`). There is no REST API; all inter-service communication uses gRPC.
* **User Model:** Each user record stores: `id` (UUID), `username`, `email`, `firstName`, `lastName`, `role` (default `ROLE_USER`), `isActive` (default `true`), `semanticIndexingEnabled` (default `false`), and auto-managed `createdAt`/`updatedAt` timestamps. The `role` field is currently a plain string; a proper enum-based RBAC system is a planned future improvement.
* **Semantic Indexing Flag:** The `semanticIndexingEnabled` boolean is a per-user opt-in that controls whether the AI/semantic indexing pipeline should process that user's documents. This flag is exposed in the gRPC `User` message and is readable by all services querying user state.
* **Active Status:** The `isActive` flag is part of the user profile and is checked by the `user-auth-service` during **login** to block inactive users from authenticating.
* **Partial Updates:** The `UpdateUser` RPC applies only the fields present in the request. String fields are applied when non-empty; boolean fields (`isActive`, `semanticIndexingEnabled`) use `google.protobuf.BoolValue` wrappers so they can be explicitly set to `false`.
* **Error Handling:** Returns standard gRPC `NOT_FOUND` status when a user does not exist, enabling callers to distinguish missing users from other errors.

#### Database Schema

Table: `users`

| Column | Type | Notes |
| --- | --- | --- |
| `id` | UUID (PK) | Auto-generated |
| `username` | `VARCHAR` | Unique, not null |
| `email` | `VARCHAR` | Unique, not null |
| `first_name` | `VARCHAR` | Not null |
| `last_name` | `VARCHAR` | Not null |
| `active` | BOOLEAN | Default `true` |
| `role` | `VARCHAR` | Default `ROLE_USER` |
| `semantic_indexing_enabled` | BOOLEAN | Default `false` |
| `created_at` | TIMESTAMP | Auto-managed |
| `updated_at` | TIMESTAMP | Auto-managed |

#### gRPC Contract (`user_mgmt.proto`)

| RPC | Request | Response |
| --- | --- | --- |
| `CreateUser` | `CreateUserRequest` | `UserResponse` |
| `GetUser` | `GetUserRequest` | `UserResponse` |
| `UpdateUser` | `UpdateUserRequest` | `UserResponse` |
| `DeleteUser` | `DeleteUserRequest` | `DeleteUserResponse` |

---

### 2. User Authentication Service (`user-auth-service`)

This service functions as the Identity Provider (IdP) and the Secure Credential Store. It hosts **two independent gRPC services** within a single Spring Boot application.

#### 2a. `UserAuthService` — Internal Authentication

Manages platform login, internal token lifecycle, and user registration/deletion. It owns a `UserMgmtClient` gRPC client to align user profile creation and deletion operations with the `user-mgmt-service` (This is done to avoid exposing the Management Service to user credentials such as username or password).

##### Registration Flow (`RegisterUser` RPC)

1. Checks that the `username` does not already exist in the `auth_users` table.
2. Calls `user-mgmt-service` via `UserMgmtClient.createUser()` to create the canonical user profile.
3. BCrypt-hashes the submitted password.
4. Persists a new `AuthUserEntity` (containing `user_id` foreign reference, `username`, and `passwordHash`) to the `auth_users` table.
5. Returns `success = true` with the new `userId`.

##### Login Flow (`Login` RPC)

1. Looks up the user's credentials in the `auth_users` table by `username`.
2. Verifies the submitted password against the stored **BCrypt** hash.
3. Calls `user-mgmt-service` via `UserMgmtClient.getUser()` to confirm the user's `isActive` status; inactive users are rejected.
4. On success, issues a signed **JWT** (HMAC-SHA256) containing the `userId` as the subject claim.
5. Returns the JWT as `internal_token` to the caller.

##### Token Validation Flow (`ValidateToken` RPC)

1. Verifies the JWT signature and expiration via `JwtTokenService`.
2. Extracts the `userId` from the token subject.
3. Confirms the user still exists in the `auth_users` table.
4. Calls `user-mgmt-service` to retrieve the current user profile and `role`.
5. Returns `isValid = true` along with the `userId` and the user's current `role`.

##### Deletion Flow (`DeleteUser` RPC)

1. Confirms the user exists in the `auth_users` table.
2. Cascades deletion of all stored service tokens (`service_tokens` table) for that `userId`.
3. Deletes the `AuthUserEntity` from `auth_users`.
4. Calls `user-mgmt-service` via `UserMgmtClient.deleteUser()` to remove the canonical user profile.
5. Returns `success = true`. If the user-mgmt-service call fails after credentials are already deleted, an error is returned indicating partial failure.

##### JWT Configuration

The signing secret (`zeta.auth.internal.hmac-secret`) and token validity window (`zeta.auth.internal.token-validity-seconds`, default `3600`) are injected from Kubernetes Secrets / environment configuration.

#### Database Schema — `auth_users`

Table: `auth_users`

| Column | Type | Notes |
| --- | --- | --- |
| `id` | UUID (PK) | Own auto-generated identity |
| `user_id` | UUID | FK reference to `user-mgmt-service` UUID; unique |
| `username` | `VARCHAR` | Unique, not null |
| `password_hash` | `VARCHAR` | BCrypt hash; not null |
| `created_at` | TIMESTAMP | Auto-managed |
| `updated_at` | TIMESTAMP | Auto-managed |

> Note: `auth_users` stores **only** credentials. All user profile data (email, name, role, etc.) lives in `user-mgmt-service`.

#### gRPC Contract (`user_auth.proto`)

| RPC | Request | Response |
| --- | --- | --- |
| `Login` | `LoginRequest` (username, password) | `LoginResponse` (success, userId, internal_token, errorMessage) |
| `ValidateToken` | `ValidateTokenRequest` (internal_token) | `ValidateTokenResponse` (isValid, userId, role) |
| `RegisterUser` | `RegisterUserRequest` (username, password, email, firstName, lastName, role, semanticIndexingEnabled) | `RegisterUserResponse` (success, userId, errorMessage) |
| `DeleteUser` | `DeleteUserRequest` (userId) | `DeleteUserResponse` (success, errorMessage) |

---

#### 2b. `IntegrationTokenService` — OAuth2 Token Vault

This is the **only** service authorized to store and read OAuth2 access tokens for external Aruba integrations.

* **Supported Services:** Designed to manage tokens for three integration types: `PEC`, `SIGN`, and `CONSERVATION`. New service types can be added by extending the `IntegrationServiceType` enum.
* **Encryption at Rest (AES-256-GCM):** Both access and refresh tokens are encrypted before being persisted to the `service_tokens` table. Each encryption call uses a randomly generated 12-byte IV, which is prepended to the ciphertext (IV + ciphertext + GCM tag, Base64-encoded). Tokens are decrypted on-the-fly in memory and are never logged or re-persisted in plaintext.
* **Key Management:** The 256-bit master key is injected at startup from a Kubernetes Secret (`zeta.auth.encryption.master-key` as Base64). Startup fails fast if the key length is incorrect.
* **Upsert Semantics:** `SaveServiceToken` performs an upsert — it updates the existing token record for a (userId, serviceType) pair if one exists, or creates a new one. This ensures idempotent token storage on account re-linking. Uniqueness is enforced at the database level via a composite unique constraint on `(user_id, service_type)`.

#### Database Schema — `service_tokens`

Table: `service_tokens`

| Column | Type | Notes |
| --- | --- | --- |
| `id` | UUID (PK) | Auto-generated |
| `user_id` | UUID | Not null |
| `service_type` | VARCHAR (enum) | `PEC`, `SIGN`, `CONSERVATION`; not null |
| `encrypted_access_token` | TEXT | AES-256-GCM ciphertext; not null |
| `encrypted_refresh_token` | TEXT | AES-256-GCM ciphertext; nullable |
| `token_expires_at` | TIMESTAMP | Not null |
| `created_at` | TIMESTAMP | Auto-managed |
| `updated_at` | TIMESTAMP | Auto-managed |

Unique constraint: `(user_id, service_type)`

#### gRPC Contract (`integration_token.proto`)

| RPC | Request | Response |
| --- | --- | --- |
| `GetServiceToken` | `GetServiceTokenRequest` (userId, serviceType) | `GetServiceTokenResponse` (found, accessToken, refreshToken, expiresAt) |
| `SaveServiceToken` | `SaveServiceTokenRequest` (userId, serviceType, accessToken, refreshToken, expiresAt) | `SaveServiceTokenResponse` (success, message) |

---

### 3. PEC Integration Service (`pec-integration-service`)

This service manages the business logic for integrating with Aruba's PEC (Posta Elettronica Certificata) system. It acts as both a **gRPC server** (exposing PEC operations to internal consumers) and an **orchestrator** of external REST calls.

* **Design: Pure Proxy (No Local Persistence):** The service intentionally holds no local copy of PEC mailbox or message data. All PEC data is fetched live from the Aruba REST API on every request. This avoids stale-data problems and keeps the service stateless with respect to PEC content.
* **OAuth2 Account Linking (`LinkPecAccount` RPC):** A caller provides a `userId` and the OAuth2 `authCode` obtained from Aruba's consent page. The service exchanges the code for an access/refresh token pair via `ArubaPecOAuth2Client.exchangeAuthorizationCode()` and persists the token pair to `user-auth-service` via the `IntegrationTokenClient` gRPC client.
* **Transparent Token Refresh:** Before every outbound API call, the service retrieves the stored token via `IntegrationTokenClient`. If the token is expired or within a **60-second buffer** of expiry (`Instant.now().getEpochSecond() >= expiresAt - 60`), it proactively exchanges the refresh token for a new pair and persists the update before proceeding.
* **Operational Flow (per PEC API request):**
    1. Calls `IntegrationTokenClient` → `user-auth-service` to retrieve (and if needed refresh) a valid OAuth2 token.
    2. Injects the token as a `Bearer` header in the outbound REST call to the Aruba PEC API via `ArubaPecApiClient`.
    3. Returns the result to the gRPC caller.
* **User Context Validation:** A `UserMgmtClient` gRPC client is provisioned in the service for validating userId existence against `user-mgmt-service`. It is available for use in higher-level operations (e.g., before account linking) that require confirming the user exists in the platform registry.

#### gRPC Contract (`pec_integration.proto`)

| RPC | Request | Response |
| --- | --- | --- |
| `GetMailboxes` | `GetMailboxesRequest` (userId) | `GetMailboxesResponse` (repeated Mailbox) |
| `GetMessages` | `GetMessagesRequest` (userId, mailboxId, pageSize, pageToken, optional startDate/endDate filters) | `GetMessagesResponse` (repeated PecMessage, nextPageToken) |
| `SendMessage` | `SendMessageRequest` (userId, mailboxId, recipientAddress, subject, bodyText, repeated documentIds) | `SendMessageResponse` (messageId, currentStatus) |
| `LinkPecAccount` | `LinkPecAccountRequest` (userId, authCode) | `LinkPecAccountResponse` (success) |

**Key gRPC types:**

* `Mailbox` (id, userId, pecAddress, MailboxStatus, createdAt, updatedAt)
* `PecMessage` (id, mailboxId, externalMessageId, senderAddress, recipientAddress, subject, MessageStatus, messageTimestamp, repeated AttachmentRef)
* `AttachmentRef` (documentId, fileName)
* `MailboxStatus`: `UNSPECIFIED`, `ACTIVE`, `INACTIVE`, `SUSPENDED`
* `MessageStatus`: `UNSPECIFIED`, `ACCEPTED`, `DELIVERED`, `FAILED`, `SENT`

#### REST Clients

| Client | Purpose |
| --- | --- |
| `ArubaPecOAuth2Client` | Exchanges OAuth2 authorization codes for token pairs; refreshes access tokens via the Aruba authorization server |
| `ArubaPecApiClient` | Proxies authenticated requests to the Aruba PEC REST API: `GET /mailboxes`, `GET /mailboxes/{id}/messages`, `POST /mailboxes/{id}/messages` |
