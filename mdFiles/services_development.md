# Services Development <img src="../mdImgs/aruba.png" alt="arubapec" width="100" align="right"/>

This document covers the services development for Platform Zeta.

## Developed Microservices

The implementation phase focused on establishing the core infrastructure required for user management, security, and the first vertical integration (PEC).
All of the developed services are located in the (`ZetaPlatform` folder).

The following three components have been developed:

### 1. User Management Service (`user-mgmt-service`)

This service acts as the central registry for all users within the Zeta Platform. It is responsible for the provisioning and lifecycle management of user identities.

* **Core Functions:** Handles User creation, updates, retrieval, and deletion (CRUD) exclusively via a **gRPC** interface (`UserManagementService`). There is no REST API; all inter-service communication uses gRPC.
* **User Model:** Each user record stores: `id` (UUID), `username`, `email`, `firstName`, `lastName`, `role` (default `ROLE_USER`), `isActive` (default `true`), `semanticIndexingEnabled` (default `false`), and auto-managed `createdAt`/`updatedAt` timestamps.
* **Semantic Indexing Flag:** The `semanticIndexingEnabled` boolean is a per-user opt-in that controls whether the AI/semantic indexing pipeline should process that user's documents. This flag is exposed in the gRPC `User` message and is readable by all services querying user state.
* **Active Status:** The `isActive` flag is part of the user profile and is checked by the `user-auth-service` during both login and token validation to block inactive users from authenticating.
* **Error Handling:** Returns standard gRPC `NOT_FOUND` status when a user does not exist, enabling callers to distinguish missing users from other errors.

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

Manages platform login and internal token lifecycle.

* **Login Flow (`Login` RPC):**
    1. Looks up the user's credentials in the `auth_users` table by username.
    2. Verifies the submitted password against the stored **BCrypt** hash.
    3. Calls `user-mgmt-service` via gRPC to confirm the user's `isActive` status.
    4. On success, issues a signed **JWT** (HMAC-SHA256) containing the `userId` as the subject claim.
    5. Returns the JWT as `internal_token` to the caller.
* **Token Validation Flow (`ValidateToken` RPC):**
    1. Verifies the JWT signature and expiration.
    2. Extracts the `userId` from the token subject.
    3. Confirms the user still exists in the `auth_users` table.
    4. Calls `user-mgmt-service` to retrieve the current user profile.
    5. Returns `isValid = true` along with the `userId` and the user's current `role`.
* **JWT Configuration:** The signing secret (`zeta.auth.internal.hmac-secret`) and token validity window (`zeta.auth.internal.token-validity-seconds`, default `3600`) are injected from Kubernetes Secrets / environment configuration.

#### gRPC Contract (`user_auth.proto`)

| RPC | Request | Response |
| --- | --- | --- |
| `Login` | `LoginRequest` (username, password) | `LoginResponse` (success, userId, internal_token, errorMessage) |
| `ValidateToken` | `ValidateTokenRequest` (internal_token) | `ValidateTokenResponse` (isValid, userId, role) |

#### 2b. `IntegrationTokenService` — OAuth2 Token Vault

This is the **only** service authorized to store and read OAuth2 access tokens for external Aruba integrations.

* **Supported Services:** Designed to manage tokens for three integration types: `PEC`, `SIGN`, and `CONSERVATION`. New service types can be added by extending the `IntegrationServiceType` enum.
* **Encryption at Rest (AES-256-GCM):** Both access and refresh tokens are encrypted before being persisted to the `service_tokens` table. Each encryption call uses a randomly generated 12-byte IV, which is prepended to the ciphertext (IV + ciphertext + GCM tag, Base64-encoded). Tokens are decrypted on-the-fly in memory and are never logged or re-persisted in plaintext.
* **Key Management:** The 256-bit master key is injected at startup from a Kubernetes Secret (`zeta.auth.encryption.master-key` as Base64). Startup fails fast if the key length is incorrect.
* **Upsert Semantics:** `SaveServiceToken` performs an upsert — it updates the existing token record for a (userId, serviceType) pair if one exists, or creates a new one. This ensures idempotent token storage on account re-linking.

#### gRPC Contract (`integration_token.proto`)

| RPC | Request | Response |
| --- | --- | --- |
| `GetServiceToken` | `GetServiceTokenRequest` (userId, serviceType) | `GetServiceTokenResponse` (found, accessToken, refreshToken, expiresAt) |
| `SaveServiceToken` | `SaveServiceTokenRequest` (userId, serviceType, accessToken, refreshToken, expiresAt) | `SaveServiceTokenResponse` (success, message) |

---

### 3. PEC Integration Service (`pec-integration-service`)

This service manages the business logic for integrating with Aruba's PEC (Posta Elettronica Certificata) system. It acts as both a **gRPC server** (exposing PEC operations to internal consumers) and an **orchestrator** of external REST calls.

* **Design: Pure Proxy (No Local Persistence):** The service intentionally holds no local copy of PEC mailbox or message data. All PEC data is fetched live from the Aruba REST API on every request. This avoids stale-data problems and keeps the service stateless with respect to PEC content.
* **OAuth2 Account Linking:** When a user links their PEC account, the service handles the full OAuth2 Authorization Code flow:
    1. Builds the Aruba authorization URI (via `ArubaPecOAuth2Client`) and redirects the user for consent.
    2. On callback, exchanges the authorization code for an access/refresh token pair.
    3. Persists the token pair (with expiry) to the `user-auth-service` via the `IntegrationTokenClient` gRPC client.
* **Transparent Token Refresh:** Before every outbound API call, the service retrieves the stored token via `IntegrationTokenClient`. If the token is expired or within a **60-second buffer** of expiry, it proactively exchanges the refresh token for a new pair and persists the update before proceeding.
* **Operational Flow (per PEC API request):**
    1. Calls `IntegrationTokenClient` → `user-auth-service` to retrieve (and if needed refresh) a valid OAuth2 token.
    2. Injects the token as a `Bearer` header in the outbound REST call to the Aruba PEC API via `ArubaPecApiClient`.
    3. Returns the result to the gRPC caller.
* **User Context Validation:** A `UserMgmtClient` gRPC client is provisioned in the service for validating userId existence against `user-mgmt-service`. It is available for use in higher-level operations (e.g., before account linking) that require confirming the user exists in the platform registry.

#### gRPC Contract (`pec_integration.proto`)

| RPC | Request | Response |
| --- | --- | --- |
| `GetMailboxes` | `GetMailboxesRequest` (userId) | `GetMailboxesResponse` (repeated Mailbox) |
| `GetMessages` | `GetMessagesRequest` (mailboxId, pageSize, pageToken, optional date filters) | `GetMessagesResponse` (repeated PecMessage, nextPageToken) |
| `SendMessage` | `SendMessageRequest` (mailboxId, recipientAddress, subject, bodyText, documentIds) | `SendMessageResponse` (messageId, currentStatus) |

**Key gRPC types:** `Mailbox` (id, userId, pecAddress, MailboxStatus), `PecMessage` (id, mailboxId, externalMessageId, sender/recipient, subject, MessageStatus, timestamp, repeated AttachmentRef).

#### REST Clients

| Client | Purpose |
| --- | --- |
| `ArubaPecOAuth2Client` | Builds authorization URI; exchanges authorization codes; refreshes access tokens via the Aruba authorization server |
| `ArubaPecApiClient` | Proxies authenticated requests to the Aruba PEC REST API: `GET /mailboxes`, `GET /mailboxes/{id}/messages`, `GET /mailboxes/{id}/messages/{msgId}`, `POST /mailboxes/{id}/messages` |
