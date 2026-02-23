# Services Development <img src="../mdImgs/aruba.png" alt="arubapec" width="100" align="right"/>

This document covers the services development for Platform Zeta.

## Developed Microservices

The implementation phase has focused on establishing the core infrastructure required for user management, security, and the first vertical integration (PEC).
All of the developed services are located in the (`ZetaPlatform` folder).

The following three components have been developed:

### 1. User Management Service (`user-mgmt-service`)

This service acts as the central registry for all users within the Zeta Platform. It is responsible for the provisioning and lifecycle management of user identities.

* **Core Functions:** Handles User creation, updates, retrieval, and deletion (CRUD).
* **Communication:** Exposes a **gRPC** interface (`UserManagementService`) used by other microservices to validate user existence and retrieve user details synchronously.

### 2. User Authentication Service (`user-auth-service`)

Aligned with the architectural design, this service functions as the Identity Provider (IdP) and the Secure Credential Store. Its primary purpose is to manage authentication and sensitive user information.

* **Token Management:** It is the **only** service authorized to store and read OAuth2 access tokens for external Aruba integrations (PEC, Sign, Preservation).
* **Security (Encryption at Rest):** Implements AES-256-GCM encryption. OAuth2 tokens are encrypted in memory before being persisted to the database and are only decrypted on-the-fly when requested by authorized services.
* **Key Management:** Utilizes Kubernetes Secrets to store the Master Key required for encryption/decryption operations.

### 3. PEC Integration Service (`pec-integration-service`)

This service manages the specific business logic for integrating with Aruba's PEC (Posta Elettronica Certificata) system.

* **Integration:** Connects to external Aruba PEC systems via REST APIs.
* **Operational Flow:**
    1. Validates the user context via the `user-mgmt-service`.
    2. Retrieves the necessary OAuth2 token from the `user-auth-service` via gRPC.
    3. Uses the token to authenticate requests to the external Aruba PEC API.
