# Advisor Booking System

Short summary of the booking solution and the prototype in this repo.

## What this is

The assignment asks for a solution that lets customers book advisor meetings from a website or mobile app, stops double-booking, saves the booking, connects to Outlook/Exchange, and sends a confirmation email.

This repo has a working prototype and a short architecture note. Node-RED was used to prototype the system flow end to end. The prototype uses PostgreSQL, Redis, RabbitMQ, and a React UI. The target design is a modular Java/Spring Boot app.

## Solution overview

- **Architecture style:** modular monolith
- **Backend:** Java 25, Spring Boot 4.1
- **Storage:** PostgreSQL
- **Locking / short-lived state:** Redis
- **Async messaging:** RabbitMQ
- **Calendar and email:** Microsoft Graph API for Outlook / Exchange Online

Main Java components:

- `BookingController`
- `BookingService`
- `CalendarService`
- `NotificationService`
- `BookingRepository`
- `AvailabilityCacheService`
- `BookingEventPublisher`
- `BookingEventHandler`

### Architecture overview

![Architecture overview](presentation/architecture/diagrams/system-overview.png)

### Booking flow

![Booking flow](presentation/architecture/diagrams/booking-flow.png)

### Node-RED prototype

Node-RED was used to try the flow quickly before the Java version.

![API Booking flow](presentation/architecture/diagrams/API-BOOKING-FLOW.jpg)

![Email Notification Service flow](presentation/architecture/diagrams/Email-Notification-Service-Flow.jpg)

## Booking flow

1. Customer sends a booking request from the website or mobile app.
2. API layer sends the request to the booking service.
3. Redis reserves the slot for a short time.
4. PostgreSQL locks the slot row and stops parallel updates.
5. Microsoft Graph checks the advisor calendar and creates the event.
6. PostgreSQL saves the booking and marks the slot as booked.
7. RabbitMQ publishes a `booking.confirmed` event.
8. Email service reads the event and sends the confirmation email.

## Race conditions

Double-booking is stopped by using:

- Redis reservation for quick visibility
- PostgreSQL row locking for correctness
- a unique constraint as the final backup

If another customer comes too late, the API returns `409 Conflict`.

## Error handling

- **Calendar unavailable:** retry a few times, then reject the booking.
- **Database unavailable:** stop fast and show a maintenance error.
- **Email failure:** booking still succeeds; email is retried later.

## Scalability

500,000 bookings per month is a moderate load. The system can scale by:

- running more API instances behind a load balancer
- keeping PostgreSQL as the main source of truth
- using Redis for fast reads and short reservations
- using RabbitMQ for background email work

## How to run

You need Docker Desktop running on your machine.

Start all services:

```bash
docker compose up -d
```

Stop all services:

```bash
docker compose down
```

## Where to open

| Service | Address |
|---------|---------|
| React UI (Node-RED) | http://localhost:1880/ui |
| React UI (Spring Boot) | http://localhost:8080 |
| Grafana | http://localhost:3000 |
| Prometheus | http://localhost:9090 |
| RabbitMQ management | http://localhost:15672 |

**Grafana login:** admin / admin

**RabbitMQ login:** guest / guest

## How to test

Open http://localhost:1880 in your browser. Pick an advisor, choose a date, pick a time slot, enter your name and email, then click **Book**. If the booking works, you will see a success message. The slot will change to "Booked".

You can also use Spring Boot UI at http://localhost:8080. It works the same way.

## Useful commands

Check if all containers are running:

```bash
docker ps
```

Check logs from Node-RED:

```bash
docker logs node-red
```

Run Spring Boot tests locally (requires Java 17+):

```bash
mvn test
```
