# Polar Bookshop — Edge Service

Polar Bookshop is a distributed bookshop application for browsing a catalog, placing orders, and processing dispatch events. This repository contains the public gateway and serves as the overview of the complete project.

## Project architecture

```mermaid
flowchart LR
    Browser --> Edge["Edge Service :9000"]
    Edge --> Catalog["Catalog Service :9001"]
    Edge --> Orders["Order Service :9002"]
    Edge --> Keycloak["Keycloak :8080"]
    Edge --> Redis["Redis :6379"]
    Catalog --> CatalogDB[("PostgreSQL: polardb_catalog")]
    Orders --> OrderDB[("PostgreSQL: polardb_order")]
    Orders --> Catalog
    Orders --> Accepted["RabbitMQ: order-accepted"]
    Accepted --> Dispatcher["Dispatcher Service :9003"]
    Dispatcher --> Dispatched["RabbitMQ: order-dispatched"]
    Dispatched --> Orders
```

1. Visitors browse books through the gateway's `/books/**` route.
2. Keycloak provides OpenID Connect login. The gateway stores sessions in Redis and relays access tokens to backend services.
3. An authenticated user submits an order through `/orders/**`. Order Service looks up the book in Catalog Service and persists an accepted or rejected order.
4. Accepted orders produce `order-accepted` events. Dispatcher Service simulates packing and labeling, then publishes `order-dispatched`.
5. Order Service consumes dispatch events and updates the order status.

The backend uses Java 17, Spring Boot, Spring Security, PostgreSQL, and RabbitMQ. The gateway uses Spring Cloud Gateway with WebFlux; catalog persistence uses JDBC and order persistence uses R2DBC. Flyway manages database migrations. Each service exposes health and Prometheus metrics through Spring Boot Actuator.

## Gateway behavior

| Route | Destination / behavior |
| --- | --- |
| `/books/**` | Catalog at `http://localhost:9001`; circuit breaker with catalog fallback |
| `/orders/**` | Orders at `http://localhost:9002` |
| `GET /user` | Current authenticated user's profile |
| `/oauth2/authorization/keycloak` | Starts login |
| `/actuator/health`, `/actuator/prometheus` | Health and metrics |

The gateway configures retries, Redis-backed rate limiting, token relay, and session persistence. Book reads and static resources are public; other application requests require authentication. Browser requests that change state must include the CSRF token.

## Run locally

Install Java 17 and Docker. Clone the four application repositories.

Before starting the applications, prepare these local dependencies:

| Dependency | Local setup |
| --- | --- |
| PostgreSQL | Port `5432`, databases `polardb_catalog` and `polardb_order`, with the configured development credentials |
| RabbitMQ | Port `5672`, with the configured development credentials |
| Redis | Port `6379` |
| Keycloak | Port `8080`, realm `PolarBookshop`, and the OAuth client described below |

Start catalog-service, order-service, and dispatcher-service in separate terminals using `.\gradlew.bat bootRun` from each repository. Then run this service:

```powershell
.\gradlew.bat bootRun
```

The gateway listens on `http://localhost:9000`. Keycloak must expose the `PolarBookshop` realm and an `edge-service` client with a callback matching `http://localhost:9000/login/oauth2/code/keycloak`.

| Setting | Default |
| --- | --- |
| `CATALOG_SERVICE_URL` | `http://localhost:9001` |
| `ORDER_SERVICE_URL` | `http://localhost:9002` |
| `SPRING_DATA_REDIS_HOST` | `localhost` |
| `SPRING_SECURITY_OAUTH2_CLIENT_PROVIDER_KEYCLOAK_ISSUER_URI` | `http://localhost:8080/realms/PolarBookshop` |

The configured OAuth client secret is a public local demo value. Override it externally with `SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_KEYCLOAK_CLIENT_SECRET` whenever using a real client.

## Related repositories

| Repository | Responsibility |
| --- | --- |
| [edge-service](https://github.com/Aymanhamad2002/edge-service) | Gateway, login, sessions, and full project overview |
| [catalog-service](https://github.com/Aymanhamad2002/catalog-service) | Book catalog and PostgreSQL persistence |
| [order-service](https://github.com/Aymanhamad2002/order-service) | Orders and asynchronous dispatch tracking |
| [dispatcher-service](https://github.com/Aymanhamad2002/dispatcher-service) | Event-driven packing and labeling |

## Build and test

Java 17 is configured in the Gradle toolchain. Use the included Gradle wrapper:

```powershell
.\gradlew.bat test
.\gradlew.bat bootJar
.\gradlew.bat bootBuildImage
```

On macOS/Linux, use `./gradlew` instead. Building container images requires Docker. Catalog and order tests include database integration tests; have Docker available for Testcontainers.

## Development configuration

The values in `src/main/resources/application.yml` are local development defaults. Demo credentials such as `user` / `password` are public, disposable values for local infrastructure; never reuse them for shared environments or real data.

Spring Boot allows environment variables to override YAML values without changing the file. For any shared or production environment, supply unique credentials externally through deployment configuration or a secret manager, and do not commit them.

See [Spring Boot externalized configuration](https://docs.spring.io/spring-boot/reference/features/external-config.html) and [OWASP secrets management](https://cheatsheetseries.owasp.org/cheatsheets/Secrets_Management_Cheat_Sheet.html).

