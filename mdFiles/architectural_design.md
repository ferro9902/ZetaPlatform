# Architectural Design <img src="../mdImgs/arubapec.png" alt="arubapec" width="150" align="right"/>

This document covers the architectural design of the Platform Zeta.

## Requirements

When engineering the architecture of the platform it must be taken into account that:

* The platform will be accessible via web.
* Integration with Aruba systems (PEC, digital signature, digital preservation) will occur via REST APIs with OAuth2 compliant authentication.

The candidate must describe, using architectural diagrams and/or written documentation:

* The overall system architecture.
* The main application components.
* The interaction modes between microservices.
* The design choices deemed most appropriate to ensure scalability, reliability, and maintainability.

### Dimensional and Functional Constraints

The architectural design must consider the following constraints:

* **Number of integrable PEC mailboxes:** ~2 million.
* **Volume of managed messages:** over 5 million per day.
* **Volume of documents sent to preservation:** ~50 GB per day.
* **The user must be able to**:
  * Upload documents directly into the portal for quick subsequent actions (e.g., signature, PEC sending, preservation).
  * Use Aruba services even without document historicization.
  * Choose whether to activate the semantic indexing of documents. If activated, it must allow user-agent (chat) interactions to interrogate their documents, both historicized ones and those used only operationally.
* **Strictly On-Premise:** The solution must be designed as totally on-premise. The use of external cloud providers or third-party AI services is not permitted.

#### Kubernetes Autoscaling Strategies
