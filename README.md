# Java Dictionary

A personal study tool for Java interview prep. Built entirely with Spring Boot.

---

## What is this?

Interview prep usually means memorizing buzzwords and hoping you can regurgitate them under pressure. That never worked for me.

What actually works is being able to explain something in your own words — not just the "right" answer, but *why* the right answer is right. That's what this tool is for.

Java Dictionary lets you store Java concepts and terms with two types of definitions:

- **Casual definition** — your own explanation. The mental model that actually makes sense to you.
- **Formal definition** — the polished, interviewer-facing answer.

The idea is that if you can write both, you actually understand the concept. If you can only write one, you know where the gap is.

---

## Features (MVP)

- Create, edit, and delete terms
- Add a casual and/or formal definition to each term
- Assign an experience level (difficulty) to each term
- Tag terms for easier categorization
- Filter and search terms by experience level or keyword
- REST API + Thymeleaf-rendered views

---

## Tech Stack

| Layer | Technology |
|---|---|
| Framework | Spring Boot 4 |
| Language | Java 17 |
| Frontend | Thymeleaf |
| Database | PostgreSQL |
| Build Tool | Maven |

The decision to use Thymeleaf instead of a separate frontend framework was intentional. This project is meant to demonstrate Spring Boot end-to-end — backend, templating, data layer, all of it.

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
mvn clean install

# Run the app
mvn spring-boot:run
```

If you're using IntelliJ, you can just hit the run button once the environment variables are set.

---

## API Reference

Base path: `/api/v1/terms`

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/v1/terms` | Get all terms. Supports `?experienceLevel=` and `?search=` query params |
| `GET` | `/api/v1/terms/{id}` | Get a term by ID |
| `GET` | `/api/v1/terms/tag/{tag}` | Get all terms with a specific tag |
| `POST` | `/api/v1/terms` | Create a new term |
| `PUT` | `/api/v1/terms/{id}` | Update an existing term |
| `DELETE` | `/api/v1/terms/{id}` | Delete a term |

The Thymeleaf views are served under `/terms` and cover the same CRUD operations.

---

## Roadmap

The MVP covers the core use case. Here's what's planned:

- **Guided learning path** — use experience level and tags to generate a structured study path, so you're not just browsing a list of terms but actually working through them in a logical order
- **Authentication** — right now this is a personal tool; auth would let it work for multiple users
- **Caching** — planned once the data layer is more stable
- **Unit tests** — full test coverage is on the list
- **CI/CD pipeline** — GitHub Actions to run tests on every push, and automated deployment
- **Deployment** — planning to host on Render (both the app and the PostgreSQL database)

---

## Project Structure

Standard Spring Boot layered architecture:

```
src/
└── main/
    └── java/com/noahparknguyen/javadictionary/
        ├── controller/   # REST API + Thymeleaf view controllers
        ├── service/       # Business logic
        ├── repository/    # Data access
        ├── model/         # Entities
        ├── dto/           # Request and response objects
        └── ...
```

---

## License

MIT