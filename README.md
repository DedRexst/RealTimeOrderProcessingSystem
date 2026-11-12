# KafkaProject — Order Processing System

Event-driven order system: 5 Spring Boot services talking over Kafka, plus an Angular admin UI.

Total first boot: about 15–20 minutes (most of it is Gradle and npm downloading dependencies). Later boots: about 3 minutes.

---

## 0. Install once

| Tool | Version | Check |
|---|---|---|
| JDK | 17 or newer | `java -version` |
| Docker + Docker Compose | any recent | `docker compose version` |
| Node.js + npm | 18.19+ (Angular 18) | `node -v` |
| PostgreSQL | any recent, on port **5433** | see step 2 |

Gradle is not needed — each service ships its own `./gradlew`.

---

## 1. Start Kafka and friends

```bash
docker compose -f DockerCompose.yaml up -d
```

This starts:

| Container | URL | What it is |
|---|---|---|
| kafka | `localhost:9092` | broker |
| schema-registry | `http://localhost:8081` | Avro schemas (AnalyticsService) |
| kafka-ui | http://localhost:8080 | browse topics and messages |
| mailpit | http://localhost:8025 | fake inbox for emails (SMTP on `1025`) |

Topics are **not** auto-created by the broker. The services create them on startup (`KafkaTopicConfig` classes), so no manual step is needed.

---

## 2. Start PostgreSQL and create 3 databases

The services expect Postgres on `localhost:5433` with three databases. Postgres is **not** in `DockerCompose.yaml`. If you have no local Postgres, run one in Docker:

```bash
docker run -d --name kafka-postgres -p 5433:5432 \
  -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=postgres postgres:16
```

Then create the databases:

```bash
docker exec -it kafka-postgres psql -U postgres -c "CREATE DATABASE kafka_auth_service_db;"
docker exec -it kafka-postgres psql -U postgres -c "CREATE DATABASE kafka_order_service_db;"
docker exec -it kafka-postgres psql -U postgres -c "CREATE DATABASE kafka_inventory_service_db;"
```

Tables are created by Liquibase when each service starts.

---

## 3. Set environment variables

Services read DB and mail credentials from env vars. Put these in each IntelliJ Run Configuration, or `export` them in the terminal before `./gradlew bootRun`.

| Service | Variables |
|---|---|
| AuthService | `DB_URL` `DB_USERNAME` `DB_PASSWORD` |
| OrderService | `DB_URL` `DB_USERNAME` `DB_PASSWORD` |
| InventoryService | `DB_URL` `DB_USERNAME` `DB_PASSWORD` |
| NotificationService | `MAIL_USERNAME` `MAIL_PASSWORD` (see step 4) |
| AnalyticsService | none |

---

## 3.1 JWT signing keys (AuthService)

AuthService signs JWTs with an RSA keypair. The keys are **not in git** (see `.gitignore`), so every clone needs its own.

**Default: do nothing.** On first start AuthService creates them itself and logs `Generated a new RSA keypair in ...`:

```
<working dir>/data/auth-keys/private-key.pem
<working dir>/data/auth-keys/public-key.pem
```

The path is relative (`security.jwt.key-directory: ./data/auth-keys`). Always start AuthService from the same folder (`cd AuthService && ./gradlew bootRun`), or it will generate a second keypair somewhere else and old tokens stop working.

**Optional: generate them by hand** (e.g. to share one keypair between machines):

```bash
mkdir -p AuthService/data/auth-keys
openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 \
  -out AuthService/data/auth-keys/private-key.pem
openssl pkey -in AuthService/data/auth-keys/private-key.pem -pubout \
  -out AuthService/data/auth-keys/public-key.pem
```

Both files must exist; if only one does, AuthService overwrites it with a fresh pair. Never commit them.

---

## 4. Boot the services — in this order

Open one terminal per service. AuthService goes first: OrderService fetches its signing keys from `http://localhost:8085/.well-known/jwks.json`.

| # | Service | Port | Command |
|---|---|---|---|
| 1 | AuthService | 8085 | `cd AuthService && ./gradlew bootRun` |
| 2 | OrderService | 8082 | `cd OrderService && ./gradlew bootRun` |
| 3 | InventoryService | 8083 | `cd InventoryService && ./gradlew bootRun` |
| 4 | AnalyticsService | 8084 | `cd AnalyticsService && ./gradlew bootRun` |
| 5 | NotificationService | — | see below |

Example for AuthService in one terminal:

```bash
cd AuthService
export DB_URL={DB_URL}
export DB_USERNAME={DB_USERNAME}
export DB_PASSWORD={DB_PASSWORD}
./gradlew bootRun
```

A service is up when the log shows `Started ...Application in X seconds`.

### NotificationService: pick one mail option

**Option A — Mailpit (recommended for local).** No real emails are sent; read them at http://localhost:8025.

```bash
cd NotificationService
export MAIL_USERNAME=noreply@local.test
export MAIL_PASSWORD=unused
./gradlew bootRun --args="--spring.mail.host=localhost --spring.mail.port=1025 \
  --spring.mail.properties.mail.smtp.starttls.enable=false \
  --spring.mail.properties.mail.smtp.starttls.required=false"
```

**Option B — real Gmail.** Use a Gmail address and a Gmail [App Password](https://myaccount.google.com/apppasswords) (not your normal password).

```bash
cd NotificationService
export MAIL_USERNAME=you@gmail.com
export MAIL_PASSWORD=your-16-char-app-password
./gradlew bootRun
```

---

## 5. Start the admin UI

```bash
cd OrderProcessingSystemAdminUI
npm install
npm start
```

Open http://localhost:5173. It **must** be port 5173 — it is the only origin the backends allow for CORS and WebSocket.

Log in with the bootstrapped admin:

- Email: `admin@example.com`
- Password: `Admin123!`

---

## 6. Check it works

1. Log in to the UI at http://localhost:5173
2. Create an order
3. Open Kafka UI at http://localhost:8080 — you should see messages in `orders.created`
4. Open Mailpit at http://localhost:8025 — you should see the notification email (Option A)

---

## Ports cheat sheet

| Port | What |
|---|---|
| 5173 | Admin UI |
| 8080 | Kafka UI |
| 8081 | Schema Registry |
| 8082 | OrderService |
| 8083 | InventoryService |
| 8084 | AnalyticsService (WebSocket `ws://localhost:8084/ws/stats`) |
| 8085 | AuthService |
| 8025 / 1025 | Mailpit web / SMTP |
| 9092 | Kafka |
| 5433 | PostgreSQL |

## Common errors

| Error | Cause | Fix |
|---|---|---|
| `Could not resolve placeholder 'DB_URL'` | Env vars not set | Do step 3 for that service |
| `database "kafka_..._db" does not exist` | DB not created | Do step 2 |
| `Connection to localhost:5433 refused` | Postgres not running | `docker start kafka-postgres` |
| OrderService 401 on every request / JWKS errors | AuthService not running | Start AuthService first |
| 401 after restarting AuthService | New keypair generated (started from a different folder) | Log out and log in again; always start from `AuthService/` (step 3.1) |
| UI shows CORS errors | UI not on port 5173 | Stop other process on 5173, rerun `npm start` |
| `UNKNOWN_TOPIC_OR_PARTITION` | Service that owns the topic not started yet | Start OrderService and InventoryService |

## Stop everything

```bash
docker compose -f DockerCompose.yaml down
docker stop kafka-postgres
```

Then `Ctrl+C` in each service terminal.
