# Shopping Cart (Spring Boot + H2 + JWT)

A small but complete shopping cart API. Java 21, Spring Boot 3.5, in-memory H2 database, JWT authentication.
Built to show SOLID, an immutable domain, an open/closed payment design and test-driven development without extra ceremony.

## Run it

Requirements: JDK 21 and Maven 3.9+.

```bash
mvn spring-boot:run          # API on http://localhost:8080
mvn test                     # unit + contract + end-to-end API tests
```

H2 console (dev only): http://localhost:8080/h2-console
JDBC URL `jdbc:h2:mem:shopdb`, user `sa`, empty password. Data lives in memory and is gone after a restart.
If you disable the console (`spring.h2.console.enabled=false`), also remove the `PathRequest.toH2Console()` line in `SecurityConfig`.

Set a real secret outside local development: `JWT_SECRET` (at least 32 characters).

## Try it with curl

```bash
# 1. Register and keep the token
TOKEN=$(curl -s -X POST localhost:8080/api/auth/register -H 'Content-Type: application/json' \
  -d '{"username":"alice","password":"password123"}' | sed -E 's/.*"token":"([^"]+)".*/\1/')

# 2. Browse (public), add to cart, view cart
curl -s localhost:8080/api/products
curl -s -X POST localhost:8080/api/cart/items -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
  -d '{"productId":2,"quantity":3}'
curl -s localhost:8080/api/cart -H "Authorization: Bearer $TOKEN"

# 3. Checkout
curl -s -X POST localhost:8080/api/orders/checkout -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
  -d '{"paymentMethod":"CARD","paymentDetails":{"cardToken":"tok_ok"}}'
```

More requests are in `requests.http`.

## API

| Method and path | Auth | Purpose |
|---|---|---|
| `POST /api/auth/register` | public | Create account, returns JWT (201) |
| `POST /api/auth/login` | public | Returns JWT |
| `GET /api/products`, `GET /api/products/{id}` | public | Browse the seeded catalog |
| `GET /api/cart` | JWT | View my cart |
| `POST /api/cart/items` | JWT | Add `{productId, quantity}` (same product merges) |
| `DELETE /api/cart/items/{productId}` | JWT | Remove an item |
| `GET /api/payment-methods` | JWT | `CARD`, `UPI`, `WALLET` (built from the registry) |
| `POST /api/orders/checkout` | JWT | `{paymentMethod, paymentDetails}`. 201 paid or awaiting confirmation, 402 declined |
| `GET /api/orders`, `GET /api/orders/{id}` | JWT | My orders only |

Errors are RFC 7807 `ProblemDetail`: 400 bad input or unsupported method, 401 not authenticated, 404 not found, 409 business rule (empty cart, duplicate username).

Demo payment rules: CARD approves any `cardToken` except one starting with `tok_fail`; UPI needs a `vpa` like `name@bank` and returns `AWAITING_CONFIRMATION`; WALLET declines orders above 500.00.

## How JWT works here

1. `register` or `login` returns a signed token (HS256, subject = username, 60 minute expiry).
2. The client sends `Authorization: Bearer <token>`.
3. `JwtAuthFilter` verifies the signature and expiry, loads the user, and authenticates the request.
4. Controllers read the username from the token (`Principal`), so a user can only reach their own cart and orders.

## Project layout

```
com.example.shop
 |- common     Money, exceptions, GlobalExceptionHandler (ProblemDetail)
 |- security   SecurityConfig, JwtService, JwtAuthFilter
 |- user       register/login, UserEntity, AppUserDetailsService
 |- product    Product (record), ProductCatalog (port), JPA adapter, controller
 |- cart       Cart/CartItem (records), CartRepository (port), JPA adapter, CartService, controller
 |- order      Order/OrderLine (records), OrderRepository (port), JPA adapter, CheckoutService, events, controller
 `- payment    PaymentProcessor (strategy), registry, Card/Upi/Wallet processors, PaymentService
```

Rule: domain records and services never import JPA. Entities and adapters are the only persistence code.

## Principles and where to see them

| Principle | Where |
|---|---|
| Single Responsibility | `JwtService` (tokens), `CheckoutService` (orchestration only), adapters (mapping), `GlobalExceptionHandler` (errors) |
| Open/Closed | Add a payment method = add one `PaymentProcessor` class. `PaymentService`, registry, `CheckoutService`, controllers and schema do not change (`PaymentProcessorRegistryTest.newProcessor_...`) |
| Liskov Substitution | `PaymentProcessorContractTest` is run against every processor |
| Interface Segregation | Small ports: `CartRepository`, `OrderRepository`, `ProductCatalog`, `PaymentProcessor` |
| Dependency Inversion | Services depend on ports; adapters implement them; constructor injection everywhere |
| Immutability | `Money`, `CartItem`, `Cart`, `OrderLine`, `Order`, `PaymentCommand`, `PaymentResult` are records with defensive copies; changes return new objects |

## Design patterns used (only the ones that fit)

| Pattern | Where |
|---|---|
| Strategy | `PaymentProcessor` and its three implementations |
| Registry / Factory | `PaymentProcessorRegistry`; factory method `Order.place(...)` |
| Repository + Adapter | Ports `CartRepository`, `OrderRepository`, `ProductCatalog`; `Jpa*` adapters; `AppUserDetailsService` |
| Facade | `CheckoutService` hides cart, order, payment and events behind one call |
| Observer | `OrderPlacedEvent` and `OrderNotificationListener` |
| Chain of Responsibility | `JwtAuthFilter` inside the Spring Security filter chain |
| DTO | Request/response records inside controllers |
| Value Object | `Money` |
| Sealed types and pattern matching | `PaymentResult` handled with an exhaustive `switch` in `CheckoutService` |

## Tests (TDD)

| Test | Kind | Needs Spring |
|---|---|---|
| `MoneyTest`, `CartTest`, `OrderTest` | unit, domain | no |
| `PaymentProcessorRegistryTest` | unit, open/closed | no |
| `Card/Upi/WalletPaymentProcessorTest` (extend the contract test) | contract | no |
| `CheckoutServiceTest` | unit with in-memory fakes | no |
| `JwtServiceTest` | unit | no |
| `ShoppingCartApiTest` | end to end (MockMvc, security, H2) | yes |

Suggested workflow when extending: write a failing test, make it pass, refactor, commit.

## Deliberate simplifications

- One currency, one cart per user, no stock tracking, no refresh tokens, a single `USER` role.
- Payment gateways are simulated. Real ones should be called outside a long transaction and confirmed by webhook (see the HLD).
- Registration returns a token immediately and there is no email verification.
- `ddl-auto: create-drop` and H2 are for development. Use Flyway and a real database for production.
