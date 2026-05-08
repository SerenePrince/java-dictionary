# Java Dictionary

A personal study tool for Java interview prep. Built with Spring Boot, PostgreSQL, and Thymeleaf.

---

## What is this?

Interview prep usually means memorizing buzzwords and hoping you can regurgitate them under pressure. That never worked
for me.

What actually works is being able to explain something in your own words — not just the "right" answer, but *why* the
right answer is right. That's what this tool is for.

Java Dictionary lets you build a personal glossary of Java concepts at multiple experience levels. Each term can have
up to four definitions — one per level — so the same concept (say, "String") can be explained simply at entry level
and precisely at senior level. Write both, and you actually understand it.

- **Casual definition** — your own explanation. The mental model that actually makes sense to you.
- **Formal definition** — the polished, interviewer-facing answer.

---

## Features

- Create, edit, and delete terms
- Multiple definitions per term — one per experience level (Entry, Junior, Intermediate, Senior)
- Tag definitions for categorization — tags are scoped per level
- Filter and search terms by experience level, keyword, or tag
- **Learning Roadmap** — a structured, guided path through Java fundamentals and OOP, with curated resources and
  research hints for each term; fill in definitions directly from the roadmap without leaving the page
- REST API + Thymeleaf-rendered UI
- Schema version control via Flyway

---

## Tech Stack

| Layer          | Technology       |
|----------------|------------------|
| Framework      | Spring Boot 4    |
| Language       | Java 17          |
| Frontend       | Thymeleaf        |
| Database       | PostgreSQL       |
| Migrations     | Flyway           |
| Build Tool     | Maven            |

The decision to use Thymeleaf instead of a separate frontend framework was intentional. This project is meant to
demonstrate Spring Boot end-to-end — backend, templating, data layer, all of it.

---

## Getting Started

### Prerequisites

- Java 17
- Maven
- PostgreSQL (running locally or via a service like Render)

### Environment Variables

The app expects two environment variables for the database connection:

```
DB_USERNAME=your_db_username
DB_PASSWORD=your_db_password
```

These are referenced in `application.properties`. Don't hardcode your credentials.

### Running the App

```bash
# Build the project
./mvnw clean install

# Run the app
./mvnw spring-boot:run
```

If you're using IntelliJ, set the environment variables in your run configuration and hit Run.

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

| Method   | Endpoint             | Description                                                                        |
|----------|----------------------|------------------------------------------------------------------------------------|
| `GET`    | `/api/v1/terms`      | Get all terms. Supports `?experienceLevel=`, `?search=`, and `?tag=` query params  |
| `GET`    | `/api/v1/terms/{id}` | Get a term by ID                                                                   |
| `POST`   | `/api/v1/terms`      | Create a new term with its first definition                                        |
| `PUT`    | `/api/v1/terms/{id}` | Upsert a definition for an existing term at the specified experience level          |
| `DELETE` | `/api/v1/terms/{id}` | Delete a term and all its definitions                                              |

The Thymeleaf views are served under `/terms` and `/roadmap` and cover the same operations through the browser UI.

---

## Project Structure

Standard Spring Boot layered architecture:

```
src/
└── main/
    ├── java/com/noahparknguyen/javadictionary/
    │   ├── config/roadmap/   # @ConfigurationProperties for roadmap.yaml
    │   ├── controller/       # REST API + Thymeleaf view controllers
    │   ├── dto/              # Request and response objects
    │   │   ├── request/
    │   │   └── response/
    │   ├── exception/        # Custom exceptions + global error handler
    │   ├── mapper/           # Entity ↔ DTO mapping
    │   ├── model/            # JPA entities (Term, TermDefinition, ExperienceLevel)
    │   ├── repository/       # Spring Data JPA repositories
    │   └── service/          # Business logic
    └── resources/
        ├── db/migration/     # Flyway SQL migrations (V1, V2, ...)
        ├── static/           # CSS and JS assets
        ├── templates/        # Thymeleaf templates
        │   ├── fragments/    # Reusable navbar, footer, form fragment
        │   ├── layout/       # Base layout
        │   ├── roadmap/      # Roadmap index + volume pages
        │   └── terms/        # Term CRUD views
        ├── application.properties
        └── roadmap.yaml      # Roadmap volumes, chapters, and entries
```

---

## Planned

- Complete the learning roadmap (DS&A, Advanced Java volumes)
- Authentication — right now this is a single-user personal tool
- Unit and integration tests
- CI/CD pipeline via GitHub Actions
- Deployment on Render

---

## License

MIT
