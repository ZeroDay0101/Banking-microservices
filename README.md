# 🏦 Banking Microservices

![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.2-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2025.1.0-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
![Thymeleaf](https://img.shields.io/badge/Thymeleaf-4.0.2-005F0F?style=for-the-badge)
![Apache Kafka](https://img.shields.io/badge/Apache%20Kafka-Confluent%207.5-231F20?style=for-the-badge&logo=apachekafka&logoColor=white)
![MongoDB](https://img.shields.io/badge/MongoDB-8-47A248?style=for-the-badge&logo=mongodb&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![OAuth2](https://img.shields.io/badge/OAuth2-JWT-EB5424?style=for-the-badge)

A back-end, event-driven banking platform built with **Spring Boot 4**, **Apache Kafka**, and **MongoDB**. Implements
the OAuth 2.0 Authorization Code flow with a dedicated authorization server, an API gateway, and three domain
microservices that communicate asynchronously through Kafka events.

## Key Features

- **OAuth 2.0 + JWT** — Full Authorization Code + PKCE flow with a custom Thymeleaf login & registration UI
- **Event-Driven Architecture** — Asynchronous inter-service communication via Kafka topics
- **Transactional Transfers** — Account-to-account transfers are `@Transactional` to prevent data inconsistency
- **Fraud Detection** — Configurable rules flag high-amount and high-frequency transactions
- **Authorization Enforcement** — Users can only access their own account data
- **MongoDB Indexed Queries** — Compound indexes on transaction lookups for fast fraud detection
- **Docker Health Checks** — Services wait for healthy Kafka and MongoDB before starting
- **Structured Logging** — Slf4j logging across all services for observability

## Architecture

```
┌──────────────────┐         OAuth2          ┌───────────────────────┐
│  Authorization   │◄───── login/consent ────│   Authorization       │
│  Server (:9000)  │── issues JWT tokens ──►│   Client / Gateway    │
│  (Spring AuthZ)  │                         │   (:8080, WebFlux)    │
└──────────────────┘                         └───────┬───────────────┘
                                                     │ TokenRelay
         ┌───────────────────────────────────────────┘
         ▼
┌──────────────────┐   Kafka Events    ┌────────────────────┐
│  Account Service │─────────────────►│ Transaction Service │
│  (:8081)         │  (deposit,        │ (:8082)             │
│                  │   transfer,       └────────┬────────────┘
│                  │   withdraw)                │ Kafka Events
│                  │                            │ (fraud suspicion)
│                  │                   ┌────────▼────────────┐
│                  │                   │   Fraud Service      │
│                  │                   │   (:8083)             │
└──────────────────┘                   └──────────────────────┘
         ▲
         │ Kafka (user-create-account)
┌────────┴─────────┐
│  Authorization   │
│  Server          │
└──────────────────┘
```

## Tech Stack

| Layer       | Technology                                   |
|-------------|----------------------------------------------|
| Language    | Java 21                                      |
| Framework   | Spring Boot 4.0.2                            |
| Cloud       | Spring Cloud 2025.1.0                        |
| Security    | Spring Authorization Server, OAuth 2.0 + JWT |
| Messaging   | Apache Kafka (Confluent 7.5)                 |
| Database    | MongoDB 8                                    |
| API Gateway | Spring Cloud Gateway (WebFlux)               |
| Templating  | Thymeleaf                                    |
| Build       | Maven (multi-module)                         |
| Containers  | Docker & Docker Compose                      |
| Tests       | JUnit 5 + Mockito                            |

## Modules

| Module                   | Description                                                                                                                                                 |
|--------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **common-lib**           | Shared event DTOs (`AccountDepositEvent`, `AccountTransferEvent`, `FraudTransactionEvent`, etc.) used across services                                       |
| **authorization-server** | OAuth 2.0 Authorization Server with user registration (form + API), custom Thymeleaf login/register page, JWT token issuance, and MongoDB-backed user store |
| **authorization-client** | Spring Cloud Gateway that acts as the OAuth 2.0 client — handles login redirect, token relay to downstream services                                         |
| **account-service**      | Core banking service — deposit, withdraw, transfer, balance queries. Publishes transaction events to Kafka                                                  |
| **transaction-service**  | Consumes Kafka events from account-service, persists an immutable transaction log in MongoDB, publishes fraud-check events                                  |
| **fraud-service**        | Consumes transaction events, evaluates fraud suspicion rules, and persists flagged transactions                                                             |

## Getting Started

### Prerequisites

- **Docker** & **Docker Compose** installed
- **Java 21** and **Maven 3.9+** (for local development only)

### Run with Docker Compose

```bash
# Clone the repository
git clone https://github.com/ZeroDay0101/Banking-microservices
cd Banking-microservices

# Build all modules
mvn clean package -DskipTests

# Start all services
docker-compose up --build
```

The stack will start the following containers:

| Service                   | URL                               |
|---------------------------|-----------------------------------|
| API Gateway (entry point) | http://localhost:8080             |
| Authorization Server      | http://localhost:9000/auth        |
| Account Service           | http://localhost:8081/account     |
| Transaction Service       | http://localhost:8082/transaction |
| Fraud Service             | http://localhost:8083/fraud       |
| Kafka                     | localhost:9092                    |
| MongoDB                   | localhost:27018                   |

### Quick Start

1. **Register a user** — Navigate to http://localhost:9000/auth/register or send:
   ```bash
   curl -X POST http://localhost:9000/auth/register \
     -H "Content-Type: application/json" \
     -d '{"username": "alice", "password": "secure123"}'
   ```

2. **Log in** — Navigate to http://localhost:8080; you'll be redirected to the OAuth2 login page.

3. **Use the API** — After login, the gateway relays your JWT saved in a browser cookie. If you wanna test the requests
   in for example postman, after login in you need to extract the SESSION cookie from the browser and include it in the
   requests. Example calls:
   ```bash
   # Check balance
   GET http://localhost:8080/account/api/accounts/balance

   # Deposit funds
   POST http://localhost:8080/account/api/accounts/deposit
   {"amount": 5000}

   # Transfer to another user
   POST http://localhost:8080/account/api/accounts/transfer
   {"recipientUsername": "bob", "amount": 100}

   # View transactions
   GET http://localhost:8080/transaction/api/transactions/user/exampleUser
   ```

## Event-Driven Flow

```
User deposits money
       │
       ▼
Account Service ──► Kafka: "account.deposit" ──► Transaction Service
                                                        │
                                                        ▼
                                                 Kafka: "fraud.check" ──► Fraud Service
                                                                              │
                                                                              ▼
                                                                     Persists flagged
                                                                     transactions
```

Every financial operation (deposit, withdrawal, transfer) in the **account-service** publishes a Kafka event. The *
*transaction-service** consumes it, creates an immutable log entry, and forwards a fraud-check event. The *
*fraud-service** evaluates the transaction and flags suspicious activity.

## API Reference

### Account Service (`/api/accounts`)

| Method | Endpoint                 | Description                                      |
|--------|--------------------------|--------------------------------------------------|
| `GET`  | `/api/accounts/{id}`     | Get account by ID (owner only)                   |
| `GET`  | `/api/accounts/balance`  | Get current user's balance                       |
| `POST` | `/api/accounts/deposit`  | Deposit funds                                    |
| `POST` | `/api/accounts/withdraw` | Withdraw funds                                   |
| `POST` | `/api/accounts/transfer` | Transfer to another user (self-transfer blocked) |

### Transaction Service (`/api/transactions`)

| Method | Endpoint                                    | Description                     |
|--------|---------------------------------------------|---------------------------------|
| `GET`  | `/api/transactions/{id}`                    | Get transaction by ID           |
| `GET`  | `/api/transactions/user/{userId}`           | Get all transactions for a user |
| `GET`  | `/api/transactions/recipient/{recipientId}` | Get all incoming transactions   |

### Fraud Service (`/api/fraud`)

| Method | Endpoint                     | Description                        |
|--------|------------------------------|------------------------------------|
| `GET`  | `/api/fraud`                 | Get all fraud records              |
| `GET`  | `/api/fraud/{transactionId}` | Get fraud record by transaction ID |
| `GET`  | `/api/fraud/user/{userId}`   | Get all fraud records for a user   |

### Authorization Server (`/auth`)

| Method | Endpoint         | Description             |
|--------|------------------|-------------------------|
| `GET`  | `/auth/login`    | Custom login page       |
| `GET`  | `/auth/register` | Registration page       |
| `POST` | `/auth/register` | Register (form or JSON) |

> All account and transaction endpoints require a valid JWT token via the `Authorization: Bearer <token>` header, or
> access through the gateway after OAuth2 login.

## Project Structure

```
Banking-microservices/
├── common-lib/                  # Shared event DTOs
├── authorization-server/        # OAuth2 Authorization Server + user registration
├── authorization-client/        # API Gateway (Spring Cloud Gateway)
├── account-service/             # Core banking operations
├── transaction-service/         # Transaction logging
├── fraud-service/               # Fraud detection
├── docker-compose.yml           # Full-stack orchestration
└── pom.xml                      # Parent POM (multi-module Maven)
```

## Configuration

Fraud detection thresholds can be tuned in `transaction-service/application.properties`:

```properties
banking.fraud.amount-threshold=10000        # Flag transactions above this amount
banking.fraud.frequency-threshold=5         # Flag if N+ transactions in the time window
banking.fraud.frequency-window-minutes=1    # Time window for frequency check
```

For production, please use ssl/tls to encrypt traffic. As well as turn on csrf in authorization client and work with
frontend.

## License
This project is for educational and portfolio purposes.
