# AlaCarte - Project Tech Stack & Versions

> Auto-generated from `build.gradle`, `gradle-wrapper.properties`, and resolved dependency tree.

---

## Runtime & Build Tools


| Component                               | Version                | Source                                        |
| --------------------------------------- | ---------------------- | --------------------------------------------- |
| **Java (source compatibility)**         | 21                     | `build.gradle` → `sourceCompatibility = '21'` |
| **JDK (build runtime)**                 | Amazon Corretto 21.0.8 | `gradle.properties` → `org.gradle.java.home`  |
| **Gradle**                              | 8.14                   | `gradle-wrapper.properties`                   |
| **Spring Dependency Management Plugin** | 1.1.5                  | `build.gradle` plugin                         |


---

## Framework & Core


| Component            | Version                   | Managed By            |
| -------------------- | ------------------------- | --------------------- |
| **Spring Boot**      | 3.2.0                     | `build.gradle` plugin |
| **Spring Framework** | 6.1.1                     | Spring Boot 3.2.0 BOM |
| **Spring Security**  | 6.2.0                     | Spring Boot 3.2.0 BOM |
| **Spring Data JPA**  | *(managed by Boot 3.2.0)* | Spring Boot BOM       |


---

## Database & ORM


| Component                         | Version           | Notes                                          |
| --------------------------------- | ----------------- | ---------------------------------------------- |
| **PostgreSQL** (database)         | Server-dependent  | Connected to `localhost:5432/Alacarte`         |
| **PostgreSQL JDBC Driver**        | 42.7.1            | Explicit in `build.gradle`                     |
| **Hibernate ORM**                 | 6.3.1.Final       | Spring Boot 3.2.0 BOM                          |
| **Hibernate Validator**           | 8.0.1.Final       | Via `spring-boot-starter-validation`           |
| **Hibernate Commons Annotations** | 6.0.6.Final       | Transitive from Hibernate ORM                  |
| **Jakarta Persistence API**       | 3.1.0             | Transitive from Hibernate ORM                  |
| **Schema Management**             | `ddl-auto=update` | `application.properties` (no Flyway/Liquibase) |


---

## AI / LLM Integration


| Component                           | Version          | Notes                                  |
| ----------------------------------- | ---------------- | -------------------------------------- |
| **LangChain4j BOM**                 | 1.9.1            | Platform BOM in `build.gradle`         |
| **LangChain4j Google AI Gemini**    | 1.9.1            | Gemini model provider                  |
| **LangChain4j OpenAI**              | 1.9.1            | OpenAI model provider                  |
| **LangChain4j Spring Boot Starter** | 1.9.1-beta17     | Spring auto-configuration              |
| **Google Cloud AI Platform**        | 3.38.0           | Vertex AI client                       |
| **Default LLM Provider**            | OpenAI (`gpt-4`) | Configured in `application.properties` |


---

## Security & Authentication


| Component           | Version                             | Notes                              |
| ------------------- | ----------------------------------- | ---------------------------------- |
| **Spring Security** | 6.2.0                               | Via `spring-boot-starter-security` |
| **JJWT (API)**      | 0.11.5                              | JWT token generation/validation    |
| **JJWT (Impl)**     | 0.11.5                              |                                    |
| **JJWT (Jackson)**  | 0.11.5                              | JSON serialization for JWT         |
| **JWT Expiration**  | 24 hours (access), 7 days (refresh) | `application.properties`           |


---

## Web & HTTP


| Component             | Version | Notes                              |
| --------------------- | ------- | ---------------------------------- |
| **Embedded Tomcat**   | 10.1.16 | Via Spring Boot                    |
| **OkHttp**            | 4.12.0  | REST client for external API calls |
| **Google API Client** | 2.2.0   | Google HTTP client                 |
| **Server Port**       | 8080    | `application.properties`           |


---

## Serialization & Data Processing


| Component            | Version | Notes                                    |
| -------------------- | ------- | ---------------------------------------- |
| **Jackson Databind** | 2.15.3  | JSON serialization (Spring Boot managed) |
| **SnakeYAML**        | 2.2     | YAML parsing (Spring Boot managed)       |
| **Apache PDFBox**    | 3.0.1   | PDF reading (menu PDF parsing)           |


---

## Developer Tools


| Component            | Version             | Notes                                |
| -------------------- | ------------------- | ------------------------------------ |
| **Lombok**           | 1.18.34             | Compile-only + annotation processor  |
| **Spring Boot Test** | 3.2.0               | Test scope                           |
| **JUnit Platform**   | *(managed by Boot)* | Test runner via `useJUnitPlatform()` |


---

## Logging


| Component           | Version | Notes                  |
| ------------------- | ------- | ---------------------- |
| **SLF4J API**       | 2.0.9   | Logging facade         |
| **Logback Classic** | 1.4.11  | Logging implementation |
| **Logback Core**    | 1.4.11  |                        |


---

## Project Metadata


| Property             | Value                              |
| -------------------- | ---------------------------------- |
| **Group**            | `com.alaCarte`                     |
| **Artifact Version** | `1.0.0`                            |
| **Main Class**       | `com.alaCarte.alaCarteApplication` |
| **Application Name** | `alaCarte`                         |
| **Repository**       | Maven Central                      |


---

## PostgreSQL-Specific Features Used

- **JSONB columns** — `meta`, `variants`, `addons`, `nutrition`, `gallery`, `permissions`, `shift_meta`, `cuisine_preferences`, `health_goals`, `qr_code`
- **TEXT[] arrays** — `cuisine_types`, `gallery_images`, `tags`, `allergies`
- **Dialect** — `org.hibernate.dialect.PostgreSQLDialect`

