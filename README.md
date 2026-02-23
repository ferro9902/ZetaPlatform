# Platform Zeta - Aruba <img src="mdImgs/aruba.png" alt="arubapec" width="100" align="right"/>

## Introduction

This project is to be implemented as part of the selection process for the AI Software Engineer position ([AI Software Engineer](https://app.ncoreplat.com/jobposition/767444/ai-software-engineer-it/rpo2)).
This project is divided in three separate sections, each developed in its separate document:

1. [Architectural design](mdFiles/architectural_design.md)
2. [Services development](mdFiles/services_development.md)
3. [AI engine and semantic indexing](mdFiles/ai_engine_semantic_indexing.md)

## Contextualization

The Platform Zeta is a cloud-native system, designed according to a microservices architecture and orchestrated via Kubernetes, to be built entirely on-premise at Aruba's data centers. The final goal is to centralize the entire ecosystem of Aruba products into a single portal, while simultaneously enabling new value-added services delivered directly from the platform.

## Business Objectives

From a business requirements perspective, the platform must allow Aruba customers to:

* Manage and integrate already purchased services (PEC, digital signature, preservation, etc.) within a single portal.
* Historicize the documents associated with these services.
* Upload documents directly into the portal for quick subsequent actions (e.g., signature, PEC sending, preservation).
* Extract value-added information from documents, including semantic information.
* If semantic indexing is activated , enable user-agent (chat) interactions to interrogate both historicized documents and those used only operationally.

## Technical Stack & Constraints

The system must be designed considering specific functional and dimensional constraints:

* **Frameworks & Tools:** The solution must be implemented using Spring Boot for application development and Docker for packaging services.
* **Integrations:** Integration with Aruba systems (PEC, signature, preservation) will occur via REST APIs with OAuth2 compliant authentication.
* **Data Volume:** The platform must handle ~2 million integrable PEC mailboxes, a volume of over 5 million messages managed per day, and ~50 GB of documents sent to preservation per day.
* **Fundamental Constraint:** The solution must be designed as totally on-premise. The use of external cloud providers or third-party AI services is not permitted.

## Assumptions & Limitations

This project implementation revolves around these assumptions:

* Out of the 5mln request described in the requirements only 5% will directly target the document ingestion pipeline and another 5% wil target the chat feature directly.
* The GPU accelerated nodes on which AI services are installed will be provisioned with Nvidia RTX Pro 6000 GPUS (exposing 96GB VRAM and the full compute capability).
* User queries will only address the text content of the uploaded documents, not any image or visual information within them (otherwise, the AI pipelines would need to be revised to leverage multi-modal models).
* Integration with external user services (provided by Aruba) happens through a linking request triggered by the user, where the Zeta application interacts with the external service to retrieve necessary information (mainly the OAuth2 token).
* At this stage of the application the User Tokens for external Aruba services are expected to be permanent (Non-expiring access tokens).
* external API services expose no Asynchronous Updates interfaces (e.g. webhook, rabbitMQ) to push update to the Zeta Platform. All updates are triggered through background polling or user-triggered actions.
