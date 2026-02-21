# Platform Zeta - Aruba ![arubapec](mdImgs/image.png)

## Introduction

This project is to be implemented as part of the selection process for the AI Software Engineer position ([AI Software Engineer](https://app.ncoreplat.com/jobposition/767444/ai-software-engineer-it/rpo2)).
This project is divided in three separate sections, each developed in its separate document:

1. [Architectural design](mdFiles/architectural_design.md)
2. [Services development](mdFiles/services_development.md)
3. [AI engine and semantic indexing](mdFiles/ai_engine_semantic_indexing.md)

## Contextualization

The Platform Zeta is a cloud-native system, designed according to a microservices architecture and orchestrated via Kubernetes, to be built entirely on-premise at Aruba's data centers[cite: 6]. The final goal is to centralize the entire ecosystem of Aruba products into a single portal, while simultaneously enabling new value-added services delivered directly from the platform.

## Business Objectives

From a business requirements perspective, the platform must allow Aruba customers to:

* Manage and integrate already purchased services (PEC, digital signature, preservation, etc.) within a single portal.
* Securely historicize access keys/tokens.
* Historicize the documents associated with these services.
* Upload documents directly into the portal for quick subsequent actions (e.g., signature, PEC sending, preservation).
* Extract value-added information from documents, including semantic information[cite: 11].
* If semantic indexing is activated , enable user-agent (chat) interactions to interrogate both historicized documents and those used only operationally.

## Technical Stack & Constraints

The system must be designed considering specific functional and dimensional constraints:

* **Frameworks & Tools:** The solution must be implemented using Spring Boot for application development and Docker for packaging services[cite: 61].
* **Integrations:** Integration with Aruba systems (PEC, signature, preservation) will occur via REST APIs with OAuth2 compliant authentication[cite: 18].
* **Data Volume:** The platform must handle ~2 million integrable PEC mailboxes, a volume of over 5 million messages managed per day, and ~50 GB of documents sent to preservation per day[cite: 39].
* **Fundamental Constraint:** The solution must be designed as totally on-premise. The use of external cloud providers or third-party AI services is not permitted[cite: 49].

## Assumptions & Limitations

This project implementation revolves around these assumptions:
