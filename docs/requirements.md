# Requirements

This document defines what the system does and the rules it follows. It is the input for design, tests and the product backlog (each user story becomes a GitHub Issue).

## 1. Actors

| Actor | Description |
|---|---|
| **Guest** | A visitor who is not logged in. Can browse public information. |
| **User** | A registered, logged-in customer. Can hold seats, pay and see their own bookings. |
| **Admin** | A staff member who manages movies, cinemas, rooms and showtimes. |

## 2. Conventions

- **Priority:** Must (MVP cannot ship without it), Should (valuable, cut first if late), Could (only if time remains).
- **Sprint:** the sprint in which the story is planned.
- **Seat states:** `AVAILABLE`, `HELD`, `SOLD`.
- **Booking states:** `PENDING`, `CONFIRMED`, `EXPIRED`, `RELEASED`, `PAYMENT_FAILED`. Transitions are defined in [state-machines.md](state-machines.md).
- All timestamps are stored and exchanged in UTC (ISO-8601). Money is an integer amount in VND.
- Business rules are referenced as `BR-xx` (section 4) and non-functional requirements as `NFR-xx` (section 5).

## 3. User stories

### Story index

| ID | Story | Actor | Priority | Sprint |
|---|---|---|---|---|
| US-01 | Register an account | Guest | Must | 1 |
| US-02 | Log in | Guest | Must | 1 |
| US-03 | Protect admin functions | Admin | Must | 1 |
| US-04 | Manage genres | Admin | Must | 2 |
| US-05 | Manage movies | Admin | Must | 2 |
| US-06 | Manage cinemas | Admin | Must | 2 |
| US-07 | Manage rooms and seat layout | Admin | Must | 2 |
| US-08 | Schedule showtimes | Admin | Must | 2 |
| US-09 | Edit or remove a showtime safely | Admin | Should | 2 |
| US-10 | Browse movies | Guest | Must | 3 |
| US-11 | View movie details | Guest | Must | 3 |
| US-12 | View showtimes of a movie | Guest | Must | 3 |
| US-13 | View the seat map of a showtime | Guest | Must | 3 |
| US-14 | Search movies by title | Guest | Should | 3 |
| US-15 | Hold seats | User | Must | 4 |
| US-16 | Expired holds are released automatically | User | Must | 4 |
| US-17 | Release my own hold | User | Should | 4 |
| US-18 | Pay for a booking | User | Must | 5 |
| US-19 | Retry requests safely | User | Must | 5 |
| US-20 | Handle failed payments | User | Must | 6 |
| US-21 | View my booking history | User | Must | 6 |
| US-22 | View a booking's details | User | Must | 6 |
| US-23 | See bookings of a showtime | Admin | Could | 6 |
| US-24 | Limit repeated failed logins | Guest | Could | 6 |
| US-25 | Stay logged in with a refresh token | User | Should | 2 |
| US-26 | Log out | User | Should | 2 |

### Authentication and authorization (Sprint 1)

#### US-01: Register an account
**Actor:** Guest | **Priority:** Must | **Sprint:** 1

As a **Guest**, I want to **register with an email, a phone number and a password** so that **I can book tickets**.

Acceptance criteria
- [ ] Registration succeeds with a valid, unused email, a valid, unused phone number and a valid password; the new account has the role `USER`.
- [ ] A duplicate email (case-insensitive) is rejected with a conflict error.
- [ ] A duplicate phone number is rejected with a conflict error.
- [ ] An invalid phone number is rejected with a validation error; a number in local format is stored in international format.
- [ ] A password shorter than the minimum is rejected with a validation error.
- [ ] The password is never stored or logged in plain text.

Rules: BR-01, BR-02, BR-04, BR-35

#### US-02: Log in
**Actor:** Guest | **Priority:** Must | **Sprint:** 1

As a **Guest**, I want to **log in with my email or my phone number, and my password** so that **I can use User features**.

Acceptance criteria
- [ ] Valid credentials (email or phone number, plus the password) return a short-lived access token and a refresh token.
- [ ] Invalid credentials return the same generic error whether the identifier or the password is wrong.
- [ ] An expired or malformed access token is rejected with `401`.

Rules: BR-03, BR-32, BR-36

#### US-03: Protect admin functions
**Actor:** Admin | **Priority:** Must | **Sprint:** 1

As an **Admin**, I want **management endpoints to be restricted to Admin accounts** so that **customers cannot change catalog data**.

Acceptance criteria
- [ ] Admin endpoints return `401` without a token and `403` for a `USER` token.
- [ ] Endpoints that require login return `401` for a Guest.
- [ ] Public endpoints work without a token.

Rules: BR-04

#### US-25: Stay logged in with a refresh token
**Actor:** User | **Priority:** Should | **Sprint:** 2

As a **User**, I want to **get a new access token without logging in again** so that **my session continues while I am active**.

Acceptance criteria
- [ ] A valid refresh token returns a new access token and a new refresh token.
- [ ] The refresh token that was just used can no longer be used.
- [ ] Presenting a refresh token that was already used revokes every refresh token of that User, forcing a new login.
- [ ] An expired, revoked or unknown refresh token is rejected with `401`.

Rules: BR-03, BR-32, BR-33, BR-34

#### US-26: Log out
**Actor:** User | **Priority:** Should | **Sprint:** 2

As a **User**, I want to **log out** so that **my session cannot be continued by someone else**.

Acceptance criteria
- [ ] Logging out revokes the presented refresh token; it can no longer be used to get new tokens.
- [ ] Logging out twice with the same token does not fail.

Rules: BR-34

### Catalog management (Sprint 2)

#### US-04: Manage genres
**Actor:** Admin | **Priority:** Must | **Sprint:** 2

As an **Admin**, I want to **create, rename and delete genres** so that **movies can be classified consistently**.

Acceptance criteria
- [ ] Genre names are unique.
- [ ] A genre that is still assigned to a movie cannot be deleted.

Rules: BR-07

#### US-05: Manage movies
**Actor:** Admin | **Priority:** Must | **Sprint:** 2

As an **Admin**, I want to **create, update and remove movies, including their age rating and genres**, so that **customers see accurate information**.

Acceptance criteria
- [ ] A movie has a title, description, duration, age rating, status and one or more genres.
- [ ] Invalid age ratings, a missing genre or a non-positive duration are rejected.
- [ ] A movie that has showtimes cannot be deleted; it can be set to `ENDED` instead.

Rules: BR-05, BR-06, BR-08

#### US-06: Manage cinemas
**Actor:** Admin | **Priority:** Must | **Sprint:** 2

As an **Admin**, I want to **create, update and remove cinemas** so that **showtimes can be placed at a location**.

Acceptance criteria
- [ ] A cinema has a name and an address.
- [ ] A cinema with rooms cannot be deleted.

#### US-07: Manage rooms and seat layout
**Actor:** Admin | **Priority:** Must | **Sprint:** 2

As an **Admin**, I want to **define rooms and their seats (row, number, type)** so that **the seat map matches the real room**.

Acceptance criteria
- [ ] A room belongs to one cinema and has a unique name within it.
- [ ] A seat is unique by row and number within its room and has a type (`STANDARD` or `VIP`).
- [ ] A room that has showtimes cannot be deleted, and its seat layout cannot change.

Rules: BR-11

#### US-08: Schedule showtimes
**Actor:** Admin | **Priority:** Must | **Sprint:** 2

As an **Admin**, I want to **schedule a movie in a room at a given time with a base price** so that **customers can book it**.

Acceptance criteria
- [ ] The end time is derived from the movie duration.
- [ ] A showtime that overlaps another in the same room (including the cleaning buffer) is rejected.
- [ ] A showtime cannot start in the past or use an `ENDED` movie.
- [ ] Creating a showtime makes every seat of the room available for that showtime.

Rules: BR-08, BR-09, BR-10

#### US-09: Edit or remove a showtime safely
**Actor:** Admin | **Priority:** Should | **Sprint:** 2

As an **Admin**, I want **safe edits and deletion of showtimes** so that **existing customers are not harmed**.

Acceptance criteria
- [ ] A showtime with any active or confirmed booking cannot be deleted, and its room and start time cannot change.
- [ ] Two Admins editing the same showtime at once: the second save is rejected with a conflict error instead of silently overwriting.
- [ ] Changing the base price does not change the price of existing bookings.

Rules: BR-12, BR-13, BR-22

### Public browsing (Sprint 3)

#### US-10: Browse movies
**Actor:** Guest | **Priority:** Must | **Sprint:** 3

As a **Guest**, I want to **browse movies filtered by status and genre** so that **I can find something to watch**.

Acceptance criteria
- [ ] The list shows title, age rating, genres and status.
- [ ] Results can be filtered by status and by genre, and are paginated.

Rules: BR-31

#### US-11: View movie details
**Actor:** Guest | **Priority:** Must | **Sprint:** 3

As a **Guest**, I want to **see the details of a movie** so that **I can decide whether to book it**.

Acceptance criteria
- [ ] Details include description, duration, age rating and genres.
- [ ] An unknown movie returns `404`.

#### US-12: View showtimes of a movie
**Actor:** Guest | **Priority:** Must | **Sprint:** 3

As a **Guest**, I want to **list the upcoming showtimes of a movie by date and cinema** so that **I can pick a session**.

Acceptance criteria
- [ ] Only showtimes that have not started are listed.
- [ ] Results can be filtered by date and cinema and are paginated.

Rules: BR-31

#### US-13: View the seat map of a showtime
**Actor:** Guest | **Priority:** Must | **Sprint:** 3

As a **Guest**, I want to **see which seats are available, held or sold** so that **I can choose seats**.

Acceptance criteria
- [ ] Every seat of the room is shown with its row, number, type, price and state.
- [ ] A seat whose hold has expired is shown as `AVAILABLE`.
- [ ] The response meets NFR-01.

Rules: BR-20, BR-22

#### US-14: Search movies by title
**Actor:** Guest | **Priority:** Should | **Sprint:** 3

As a **Guest**, I want to **search movies by part of the title** so that **I can find a movie quickly**.

Acceptance criteria
- [ ] Search is case-insensitive and combines with the genre and status filters.

### Seat holding and payment (Sprints 4 to 6)

#### US-15: Hold seats
**Actor:** User | **Priority:** Must | **Sprint:** 4

As a **User**, I want to **select seats and have them held for a limited time** so that **I have time to pay without losing them**.

Acceptance criteria
- [ ] Only a logged-in User can hold seats.
- [ ] Only `AVAILABLE` seats can be held, and only before sales close for the showtime.
- [ ] The request is all-or-nothing: if any requested seat is unavailable, no seat is held and the response says so.
- [ ] A successful hold creates a `PENDING` booking with an expiry time and the total price.
- [ ] Two Users holding the same seat at the same moment: exactly one succeeds, the other gets a conflict error.
- [ ] Holding more than the maximum number of seats is rejected.
- [ ] A User who already has an active `PENDING` booking cannot start another.

Rules: BR-14, BR-15, BR-16, BR-17, BR-18, BR-19, BR-22, BR-28
Non-functional: NFR-02, NFR-03, NFR-04

#### US-16: Expired holds are released automatically
**Actor:** User | **Priority:** Must | **Sprint:** 4

As a **User**, I want **seats I could not pay for in time to be released** so that **other customers can book them and I am not left with a stuck booking**.

Acceptance criteria
- [ ] When the hold time passes without payment, the booking becomes `EXPIRED` and its seats return to `AVAILABLE`.
- [ ] An expired hold cannot be paid, even if the cleanup job has not run yet.
- [ ] Only rows that are actually expired are touched; held seats of other bookings are unaffected.
- [ ] Release happens within the time limit in NFR-05.

Rules: BR-20, BR-21

#### US-17: Release my own hold
**Actor:** User | **Priority:** Should | **Sprint:** 4

As a **User**, I want to **give up my pending hold** so that **I can choose different seats**.

Acceptance criteria
- [ ] A User can release only their own `PENDING` booking.
- [ ] The booking becomes `RELEASED` and its seats return to `AVAILABLE` immediately.
- [ ] A `CONFIRMED` booking cannot be released (cancellation is out of scope).

Rules: BR-30

#### US-18: Pay for a booking
**Actor:** User | **Priority:** Must | **Sprint:** 5

As a **User**, I want to **pay for my held seats** so that **my booking is confirmed**.

Acceptance criteria
- [ ] Only the owner of a non-expired `PENDING` booking can pay for it.
- [ ] The payment amount equals the booking total.
- [ ] On success the booking becomes `CONFIRMED`, the seats become `SOLD`, and a unique booking code is issued.
- [ ] Confirming the booking and marking the seats sold happen atomically.
- [ ] Payment is simulated through a gateway stub that can be configured to succeed or fail.

Rules: BR-23, BR-24, BR-26, BR-27, BR-28

#### US-19: Retry requests safely
**Actor:** User | **Priority:** Must | **Sprint:** 5

As a **User**, I want **a retried request to be harmless** so that **a network glitch never creates a duplicate hold or a double charge**.

Acceptance criteria
- [ ] Hold and pay requests require an `Idempotency-Key` header.
- [ ] Repeating a request with the same key and the same content returns the original result and causes no new effect.
- [ ] Reusing a key with different content is rejected.
- [ ] Keys of different Users never collide.

Rules: BR-28, BR-29

#### US-20: Handle failed payments
**Actor:** User | **Priority:** Must | **Sprint:** 6

As a **User**, I want to **retry a failed payment within the hold time** so that **one failure does not lose my seats**.

Acceptance criteria
- [ ] A failed attempt keeps the booking `PENDING` while attempts and hold time remain.
- [ ] After the maximum number of failed attempts the booking becomes `PAYMENT_FAILED` and its seats are released.
- [ ] Every attempt is recorded with its outcome.

Rules: BR-23, BR-25

#### US-21: View my booking history
**Actor:** User | **Priority:** Must | **Sprint:** 6

As a **User**, I want to **list my bookings** so that **I can see what I have booked**.

Acceptance criteria
- [ ] Only the User's own bookings are returned, newest first, paginated.
- [ ] Each entry shows movie, showtime, seats, total, state and booking code (if confirmed).

Rules: BR-30, BR-31

#### US-22: View a booking's details
**Actor:** User | **Priority:** Must | **Sprint:** 6

As a **User**, I want to **open one booking** so that **I can check its seats, price and payment attempts**.

Acceptance criteria
- [ ] Another User's booking returns `404`.

Rules: BR-30

#### US-23: See bookings of a showtime
**Actor:** Admin | **Priority:** Could | **Sprint:** 6

As an **Admin**, I want to **see the confirmed bookings and occupancy of a showtime** so that **I can monitor sales**.

Acceptance criteria
- [ ] Shows confirmed bookings, sold seat count and occupancy percentage.

#### US-24: Limit repeated failed logins
**Actor:** Guest | **Priority:** Could | **Sprint:** 6

As the **system owner**, I want to **slow down repeated failed logins** so that **password guessing is harder**.

Acceptance criteria
- [ ] After a number of consecutive failures from the same account or address, further attempts are temporarily rejected.

## 4. Business rules

### Accounts and access

| ID | Rule |
|---|---|
| BR-01 | An email identifies one account and is unique, ignoring letter case. |
| BR-02 | A password has at least 8 characters. |
| BR-03 | An access token is valid for 15 minutes. |
| BR-04 | A registered account always has the role `USER`. Admin accounts are created only by seed data or configuration, never through public registration. |
| BR-32 | A refresh token is valid for 7 days. |
| BR-33 | A refresh token can be used once. Using it issues a new access token and a new refresh token and invalidates the old one (rotation). If an already used refresh token is presented again, all refresh tokens of that User are revoked. |
| BR-34 | Refresh tokens are stored only as hashes. Logging out revokes the presented refresh token. |
| BR-35 | A phone number identifies one account and is unique. It is stored in international format (E.164, for example `+84901234567`); numbers typed in local format (`0901234567`) are converted. The MVP accepts Vietnamese mobile numbers only. The number is not verified by SMS. |
| BR-36 | A User logs in with an email or a phone number plus the password. The identifier is treated as an email when it contains `@`, otherwise as a phone number. Failed logins never reveal which part was wrong. |

### Catalog

| ID | Rule |
|---|---|
| BR-05 | A movie has exactly one age rating: `P` (all ages), `K` (under 13 only with a parent), `T13`, `T16` or `T18`. The rating is informational and is not checked against the User at booking time. |
| BR-06 | A movie has between 1 and 5 genres. |
| BR-07 | Genre names are unique, ignoring letter case. |
| BR-08 | A movie status is `COMING_SOON`, `NOW_SHOWING` or `ENDED`. Showtimes can be scheduled only for movies that are not `ENDED`. |
| BR-09 | Two showtimes in the same room cannot overlap. A 15-minute cleaning buffer is required after each showtime. |
| BR-10 | A showtime cannot be created with a start time in the past. |
| BR-11 | A seat is unique by row and number within its room. Seat types are `STANDARD` and `VIP`. |
| BR-12 | A showtime with an active (`PENDING`) or `CONFIRMED` booking cannot be deleted, and its room and start time cannot change. |
| BR-13 | Concurrent edits of the same record are detected with a version check; the later save fails with a conflict error. |

### Seat holding

| ID | Rule |
|---|---|
| BR-14 | Only a seat in state `AVAILABLE` can be held. |
| BR-15 | A booking has at most 8 seats, all in the same showtime. |
| BR-16 | A hold lasts 10 minutes from the moment it succeeds. |
| BR-17 | A User can have at most one `PENDING` booking at a time. |
| BR-18 | Sales for a showtime close 15 minutes before it starts; seats can no longer be held after that. |
| BR-19 | Holding is all-or-nothing: if any requested seat is unavailable, no seat is held. |
| BR-20 | A hold that has passed its expiry time is treated as expired immediately: it cannot be paid and its seats count as `AVAILABLE`, even if the cleanup job has not yet updated the rows. |
| BR-21 | When a hold expires, the booking becomes `EXPIRED` and its seats return to `AVAILABLE`. |

### Pricing

| ID | Rule |
|---|---|
| BR-22 | The price of a seat is the showtime base price plus a surcharge by seat type (`STANDARD` has none, `VIP` has a fixed surcharge). The booking total is the sum over its seats. Prices are copied into the booking when the hold succeeds and never change afterwards. |

### Payment

| ID | Rule |
|---|---|
| BR-23 | Payment is simulated by a gateway stub; no real money moves. |
| BR-24 | Only the owner of a `PENDING`, non-expired booking can pay for it. |
| BR-25 | A booking allows at most 3 payment attempts, all within the hold time. After the third failure the booking becomes `PAYMENT_FAILED` and its seats are released. |
| BR-26 | When payment succeeds, the booking becomes `CONFIRMED`, its seats become `SOLD` permanently, and a unique booking code is generated. |
| BR-27 | The amount of a payment must equal the booking total. |

### Idempotency

| ID | Rule |
|---|---|
| BR-28 | Hold and payment requests require an `Idempotency-Key`. The same key with the same content returns the stored result and has no further effect. |
| BR-29 | A key is scoped to the User who sent it, is remembered for 24 hours, and reusing it with different content is rejected. |

### Data access and listing

| ID | Rule |
|---|---|
| BR-30 | A User can see and act only on their own bookings. Another User's booking is reported as not found. |
| BR-31 | List endpoints are paginated: default page size 20, maximum 100. |

## 5. Non-functional requirements

All figures are targets to be measured and reported in [testing.md](testing.md); the measuring machine and the number of runs are recorded with each result.

| ID | Category | Requirement |
|---|---|---|
| NFR-01 | Performance | Reading a seat map has p95 under 200 ms on the seed dataset (at least 500,000 seat-state rows). |
| NFR-02 | Correctness | With 200 concurrent hold requests for the same seat, exactly one succeeds and the number of double bookings is 0. |
| NFR-03 | Performance | Under the same 200 concurrent requests, the hold endpoint has p95 under 500 ms. |
| NFR-04 | Reliability | A hold or payment request repeated with the same `Idempotency-Key` produces exactly one booking effect. |
| NFR-05 | Reliability | An expired hold is released within 60 seconds after its expiry time. |
| NFR-06 | Security | Passwords are stored as BCrypt hashes and refresh tokens as hashes. No secret is stored in the repository or its history. Error responses never contain stack traces. |
| NFR-07 | Security | Every endpoint has an explicit access rule, verified by tests for unauthorized and forbidden access. |
| NFR-08 | Quality | Core business logic (seat holding, expiry, state transitions, pricing, idempotency) has at least 80% line coverage. CI runs all tests on every pull request. |
| NFR-09 | Observability | State changes of bookings and payments are logged with a correlation id; hold success and failure counts are exposed as metrics. |
| NFR-10 | Portability | The whole system starts from a clean checkout with `docker compose up`. |

## 6. Out of scope

Deliberately excluded from the MVP and candidates for a later phase:

- Movie ratings and reviews
- Cancelling confirmed bookings and refunds
- Real email delivery
- Vouchers and food or drink add-ons
- A real payment gateway
- Phone number verification by SMS or one-time code
- Age verification against the User's date of birth