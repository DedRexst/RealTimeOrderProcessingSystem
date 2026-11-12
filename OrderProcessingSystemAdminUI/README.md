# OrderFlow Admin UI

Angular 18 admin console for the Kafka order-processing system. It is structured like `admin-ui`: an NgModule app with `services/` + `services/models/`, `module/app-routing.module.ts`, a class-based `AuthInterceptor`, guards, `AlertService` and `LoaderService`.

## Run

```bash
npm install
npm start          # http://localhost:5173  (must be 5173: it is the only origin the backends allow for CORS/WS)
npm test           # Karma + ChromeHeadless
npm run lint
npm run build      # production build -> dist/order-processing-admin
```

Sign in with the bootstrapped admin, `admin@example.com` / `Admin123!`, configured in `AuthService/application.yml`.

## Backends used

| Service | Endpoint | Used for |
|---|---|---|
| AuthService | `:8085/api/v1/auth/*`, `/users/me`, `/clients` | login, rotating refresh, profile, OAuth client management |
| OrderService | `:8082/api/v1/orders`, `/catalog/*` | order list/details/create/status changes, products & customers |
| AnalyticsService | `ws://:8084/ws/stats` | live KPIs, charts, activity feed, order status history |
| Kafka UI / Mailpit | `:8080` / `:8025` | linked from the sidebar |

Endpoints live in `src/environments/environment.ts` (`Constants.Default`).

## Pages

- **Dashboard**: KPI tiles, order throughput, currency mix, status funnel, top products, transition latency, top reasons, and a live event feed.
- **Orders**: server-paged list with status and customer filters, kept in the URL. It refreshes live when the WebSocket reports changes. A **New order** modal sits alongside it.
- **Order details**: a progress stepper and a status timeline built from the event stream. Admins can change the status; only transitions the backend allows are offered.
- **Catalog**: products and customers.
- **API clients** (ADMIN): register a client, show its one-time secret with a curl snippet, rotate the secret, enable or disable the client.
- **Profile**: `/users/me` and session info.

## Notes

- Access tokens refresh automatically. If the token is expired before a request, it is refreshed first. If a request gets a 401, the token is refreshed once and the request retried. Concurrent requests share one refresh call, because the backend treats reuse of a refresh token as theft.
- `tsconfig.json` uses `moduleResolution: "node"`, as `admin-ui` does. ngx-bootstrap 18's `exports` map points at typings it doesn't ship, so `"bundler"` resolution fails.
- The order status history comes from AnalyticsService's in-memory stream: live events, plus the last 50 replayed on connect. It is not a persistent audit log.
