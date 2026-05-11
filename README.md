# Java Dictionary

A personal study tool for Java interview prep. Built with Spring Boot, PostgreSQL, and Thymeleaf.

---

## What is this?

Interview prep usually means memorizing buzzwords and hoping you can regurgitate them under pressure. That never worked for me.

What actually works is being able to explain something in your own words — not just the "right" answer, but *why* the right answer is right. That's what this tool is for.

Java Dictionary lets you build a personal glossary of Java concepts. Each term has two definitions:

- **Casual definition** — your own explanation. The mental model that actually makes sense to you.
- **Formal definition** — the polished, interviewer-facing answer.

Write both, and you actually understand the concept.

Terms are grouped by name so the same concept can appear multiple times — once per source book and chapter — letting you track how your understanding evolves as you work through different material. You can also add terms manually, outside of any book.

---

## Features

- Create, edit, and delete terms manually
- Flat term model: each entry has a casual and formal definition, a source (book + chapter), and tags
- Multiple entries per term name — one per source, grouped together for display
- Tag entries for categorization and filtering
- Filter and search terms by keyword, tag, or source book
- **Learning Roadmap** — a YAML-driven structured path through Java fundamentals, with curated resources and research hints for each term; submit definitions directly from the roadmap without leaving the page
- Duplicate protection — manual terms deduplicated by name, book-sourced terms by (name, book, chapter)
- Smart delete redirect — stays on the group page if other entries exist, returns to index if the last one is deleted
- REST API with OpenAPI/Swagger UI documentation
- Thymeleaf-rendered UI
- Schema version control via Flyway

---

## Tech Stack

| Layer          | Technology                        |
|----------------|-----------------------------------|
| Framework      | Spring Boot 4                     |
| Language       | Java 17                           |
| Frontend       | Thymeleaf + Bootstrap 5           |
| Database       | PostgreSQL                        |
| Migrations     | Flyway                            |
| Build Tool     | Maven                             |
| API Docs       | SpringDoc OpenAPI (Swagger UI)    |

The decision to use Thymeleaf instead of a separate frontend framework was intentional. This project is meant to demonstrate Spring Boot end-to-end — backend, templating, data layer, all of it.

---

## Getting Started

### Prerequisites

- Java 17
- Maven
- PostgreSQL (running locally or via a service like Render)

### Profiles

The app uses Spring profiles to separate local and production configuration:

| Profile | File                          | When it's used          |
|---------|-------------------------------|-------------------------|
| `dev`   | `application-dev.properties`  | Local development       |
| `prod`  | `application-prod.properties` | Deployment / production |

`application.properties` holds shared settings common to both profiles.

### Environment Variables

**Dev profile** — set `DB_USERNAME` and `DB_PASSWORD` in your IntelliJ run configuration or shell.
The database URL is hardcoded to `localhost:5432/java_dictionary`.

**Prod profile** — set all three as secrets in your deployment service:

```
DB_URL=jdbc:postgresql://<host>:<port>/<database>
DB_USERNAME=your_db_username
DB_PASSWORD=your_db_password
```

Never hardcode credentials.

### Running the App

```bash
# Local development (dev profile)
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

If you're using IntelliJ, add `spring.profiles.active=dev` to the Active profiles field
(or set it as a VM option: `-Dspring.profiles.active=dev`) in your run configuration alongside
the `DB_USERNAME` and `DB_PASSWORD` environment variables.

**Activating the prod profile on your deployment service** — add this environment variable:

```
SPRING_PROFILES_ACTIVE=prod
```

Spring Boot reads this automatically, no code changes needed.

### Database Migrations

Schema is managed by Flyway. Migrations run automatically on startup. If you ever need to repair the schema history
(e.g. after editing a migration file that was already applied):

```bash
./mvnw flyway:repair
```

Flyway reads `DB_USERNAME` and `DB_PASSWORD` directly from your shell environment.

---

## API Reference

Base path: `/api/v1/terms`

| Method   | Endpoint                    | Description                                                                              |
|----------|-----------------------------|------------------------------------------------------------------------------------------|
| `GET`    | `/api/v1/terms`             | Get all term groups. Supports `?search=`, `?tag=`, and `?book=` query params             |
| `GET`    | `/api/v1/terms/slug/{slug}` | Get a single term group by slug (all entries sharing that name)                          |
| `GET`    | `/api/v1/terms/{id}`        | Get a single term entry by database ID                                                   |
| `POST`   | `/api/v1/terms`             | Create a new manual term (no source book or chapter)                                     |
| `PUT`    | `/api/v1/terms/{id}`        | Update a term entry. Manual terms: all fields. Book-sourced terms: definitions only      |
| `DELETE` | `/api/v1/terms/{id}`        | Delete a single term entry                                                               |

The full interactive API reference is available at `/swagger-ui.html` when the app is running.

The Thymeleaf views are served under `/terms` and `/roadmap` and cover the same operations through the browser UI.

---

## Data Model

Terms are stored flat. There is no nested definition or experience-level structure.

| Field             | Type     | Notes                                                  |
|-------------------|----------|--------------------------------------------------------|
| `id`              | Long     | Primary key                                            |
| `name`            | String   | Display name (e.g. "Garbage Collection")               |
| `slug`            | String   | URL-safe identifier derived from name (e.g. "garbage-collection") |
| `casualDefinition`| String   | Plain-language explanation                             |
| `formalDefinition`| String   | Precise, interview-ready definition                    |
| `sourceBook`      | String   | Source book title; `null` for manual terms             |
| `sourceChapter`   | String   | Chapter within the source book; `null` for manual terms|
| `tags`            | Set      | Keyword tags (stored in `term_tags` table)             |

A **manual term** has both `sourceBook` and `sourceChapter` set to `null`. The database enforces uniqueness with two separate constraints: a composite unique constraint on `(name, source_book, source_chapter)` for book-sourced terms, and a partial unique index on `name` (where both source columns are null) for manual terms — because SQL treats `NULL != NULL` and a standard unique constraint cannot cover this case.

The same term name can appear in multiple rows when it is sourced from different books or chapters. The service layer groups these rows by `slug` before returning them to the UI or API.

---

## Roadmap

The learning roadmap is defined entirely in `src/main/resources/roadmap.yaml`. No code changes are needed to add new volumes, chapters, or terms — just edit the YAML.

Each volume maps to a source book. Each chapter within a volume maps to a chapter in that book. Each entry specifies the term to define, suggested tags, external learning resources, and guided research hints (what, why, how).

When a definition is submitted from the roadmap UI, it is saved as a book-sourced term with `sourceBook` and `sourceChapter` set from the YAML config. If a definition already exists for that (term, book, chapter) combination, the UI asks for confirmation before overriding it.

---

## Project Structure

Standard Spring Boot layered architecture:

```
src/
└── main/
    ├── java/com/noahparknguyen/javadictionary/
    │   ├── config/
    │   │   ├── OpenApiConfig.java        # SpringDoc API metadata
    │   │   └── roadmap/                  # @ConfigurationProperties for roadmap.yaml
    │   │       ├── RoadmapProperties.java
    │   │       ├── VolumeConfig.java
    │   │       ├── ChapterConfig.java
    │   │       ├── EntryConfig.java
    │   │       ├── ResourceConfig.java
    │   │       └── HintConfig.java
    │   ├── controller/
    │   │   ├── TermController.java       # REST API (/api/v1/terms)
    │   │   ├── TermViewController.java   # Thymeleaf UI (/terms)
    │   │   └── RoadmapController.java    # Thymeleaf UI (/roadmap)
    │   ├── dto/
    │   │   ├── request/
    │   │   │   └── CreateTermRequest.java
    │   │   └── response/
    │   │       ├── TermResponse.java
    │   │       ├── TermGroupView.java
    │   │       ├── RoadmapChapterView.java
    │   │       └── RoadmapEntryView.java
    │   ├── exception/
    │   │   ├── GlobalExceptionHandler.java
    │   │   ├── ResourceNotFoundException.java
    │   │   ├── DuplicateResourceException.java
    │   │   └── ErrorResponse.java
    │   ├── mapper/
    │   │   └── TermMapper.java           # Entity ↔ DTO conversion + grouping logic
    │   ├── model/
    │   │   └── Term.java                 # JPA entity
    │   ├── repository/
    │   │   └── TermRepository.java       # Spring Data JPA + custom JPQL queries
    │   └── service/
    │       ├── TermService.java          # Term CRUD + uniqueness rules
    │       └── RoadmapService.java       # Config-DB bridge for roadmap views
    └── resources/
        ├── db/migration/                 # Flyway SQL migrations (V1, V2, V3)
        ├── static/css/                   # Custom styles (main.css)
        ├── templates/
        │   ├── fragments/                # Navbar, footer, shared term form
        │   ├── layout/                   # Base Thymeleaf layout
        │   ├── roadmap/                  # Roadmap index + volume pages
        │   └── terms/                    # Term index, detail, create, edit
        ├── application.properties        # Shared config (DB driver, Flyway, SpringDoc)
        ├── application-dev.properties    # Local dev overrides (URL, SQL logging)
        ├── application-prod.properties   # Production overrides (env vars, no SQL logging)
        └── roadmap.yaml                  # Roadmap volumes, chapters, entries, and hints
```

---

## Planned

- Complete the learning roadmap (DS&A, Collections, Concurrency, Advanced Java volumes)
- Unit and integration tests
- CI/CD pipeline via GitHub Actions
- Deployment on Render
- Authentication — right now this is a single-user personal tool

---

## License

MIT
