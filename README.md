# Cinema Ticket Booking Platform

A backend for online cinema ticketing, built around one hard problem: **two people must never be able to buy the same seat**, even when they click at the same moment.

> **Status:** 🚧 In development. Requirements and design are written; implementation starts with Sprint 1. This README is updated as each sprint closes.

## The problem

Online seat booking looks simple until traffic arrives. When many users pick the same seat at the same moment, a naive implementation sells it twice. Users also need time to pay without losing the seat, and a flaky network must not turn a retried payment into a double charge.

This project focuses on getting those three things right and proving it with tests and measurements:

1. **Seat holding with expiry:** a user holds seats for a limited time; unpaid holds are released automatically.
2. **No double booking under concurrency:** enforced at the database level and verified by concurrent tests.
3. **Idempotent payment and booking:** retried requests never create duplicates.

## Scope

**In scope (MVP)**

- Guest: browse movies, showtimes and the seat map of a showtime.
- User: register, log in, hold seats, pay (simulated), view own booking history.
- Admin: manage movies, cinemas, rooms, seats and showtimes.

**Out of scope for the MVP** (possible Phase 2): booking cancellation and refunds, real email delivery, vouchers, food and drink add-ons, real payment gateway.

## Tech stack

| Area | Choice |
|---|---|
| Language / framework | Java 21, Spring Boot (current stable line) |
| Database | PostgreSQL, schema managed with Flyway |
| Security | Spring Security, JWT, BCrypt |
| API docs | OpenAPI / Swagger UI (springdoc) |
| Testing | JUnit 5, Mockito, Testcontainers, k6 (load tests) |
| Delivery | Docker (multi-stage), docker-compose, GitHub Actions |

Why these choices, and what was rejected, is recorded in the [architecture decision records](docs/adr/). Redis and Kafka are deliberately **not** in the stack yet: they will be added only if measurements show a real bottleneck.

## Architecture in one paragraph

A **modular monolith**: one deployable application split into modules (`auth`, `movie`, `cinema`, `showtime`, `reservation`, `booking`, `payment`), each layered as controller → service → repository. A single database keeps transactions simple, which matters most for seat holding. Splitting into microservices would add network failure modes without solving any problem this project has.

## Documentation

| Document | Purpose |
|---|---|
| [Requirements](docs/requirements.md) | User stories, business rules, non-functional requirements |
| [State machines](docs/state-machines.md) | Lifecycle of seats, bookings and payments |
| [Data model (ERD)](docs/erd.md) | Tables, relationships and constraints |
| [Architecture](docs/architecture.md) | Modules, layers and sequence diagrams of the core flows |
| [Architecture decisions](docs/adr/) | Why each major technical choice was made |

Interactive API documentation (Swagger UI) will be available at `/swagger-ui` once the application runs.

## Roadmap

| Sprint | Goal | Status |
|---|---|---|
| 1 | Project skeleton, migrations, authentication, authorization, CI | Planned |
| 2 | Admin management APIs, validation, unified errors, API docs | Planned |
| 3 | Public browsing APIs, seat map, pagination, query optimization | Planned |
| 4 | Seat holding, double-booking prevention, hold expiry, concurrency tests | Planned |
| 5 | Booking, simulated payment, idempotency, first load test | Planned |
| 6 | Failed payments, booking history, Docker, CD, security review | Planned |

Work is tracked with GitHub Issues, Projects and Milestones.

## Getting started

Setup instructions will be added when the first runnable version lands in Sprint 1.

## Conventions

- Actors are always named **Guest** (not logged in), **User** (logged in) and **Admin**.
- Branching: `main` is always working; changes go through feature branches and pull requests.
- Commits follow [Conventional Commits](https://www.conventionalcommits.org/) (`feat:`, `fix:`, `docs:`, `test:`, `refactor:`, `chore:`).
- Database changes are new Flyway migrations; existing migrations are never edited.
