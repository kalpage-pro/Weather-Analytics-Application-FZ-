# Weather-Analytics-Application-FZ-
for the interview selection assignment for Fidenz

# Weather Comfort Analytics (Fidenz Assignment)

A secure weather analytics application: fetches live weather per city from
OpenWeatherMap, computes a custom Comfort Index, ranks cities most-to-least
comfortable, caches results server-side, and gates the whole dashboard behind
Auth0 authentication (login, MFA via email, whitelisted signups only).

- **Backend**: Spring Boot (Java 17), REST API, Caffeine caching, Auth0 JWT resource server
- **Frontend**: React + TypeScript (Vite), Auth0 React SDK

## Project structure


backend/    Spring Boot API (Maven)
frontend/   React + TypeScript dashboard (Vite)


## 1. Backend setup

1. **OpenWeatherMap API key**: sign up at https://openweathermap.org/, grab a
   key from "My API Keys". New keys can take up to ~2 hours to activate.
2. **Auth0 setup** - see section 3 below first, then come back for these env vars.
3. Set environment variables (PowerShell shown; use `export` on macOS/Linux):
   ```powershell
   $env:OWM_API_KEY="your_openweathermap_key"
   $env:AUTH0_ISSUER_URI="https://YOUR_DOMAIN.us.auth0.com/"  
   $env:AUTH0_AUDIENCE="https://your-api-identifier"
   $env:FRONTEND_ORIGIN="http://localhost:5173"               
   ```
4. Run it:
   ```
   cd backend
   mvn spring-boot:run
   ```
   API comes up on `http://localhost:8080`.
5. Endpoints:
   - `GET /api/cities` - all configured cities, weather, Comfort Index, and rank (requires a valid Auth0 Bearer token)
   - `GET /api/debug/cache-status` - per-cache hit/miss counts and currently cached keys (also requires auth)

`src/main/resources/cities.json` is the file provided with the assignment
(extended to 10 entries to meet the assignment's minimum). `Temp`/`Status`
fields in that file are sample/reference values only - the app always calls
OpenWeatherMap live for real data, never trusts those fields.

## 2. Frontend setup

1. Fill in `frontend/.env` (see section 3 for where these values come from):
   ```
   VITE_API_BASE_URL=http://localhost:8080
   VITE_AUTH0_DOMAIN=YOUR_DOMAIN.us.auth0.com
   VITE_AUTH0_CLIENT_ID=your_client_id
   VITE_AUTH0_AUDIENCE=https://your-api-identifier
   ```
2. Install and run:
   ```
   cd frontend
   npm install
   npm run dev
   ```
3. Open `http://localhost:5173`. You'll land on a login screen; log in with an
   Auth0-whitelisted account to reach the dashboard.

## 3. Auth0 setup (Part 2)

Both the frontend and backend need matching Auth0 configuration.

1. Create an Auth0 tenant, then create:
   - An **Application** (Single Page Application) for the React frontend -
     note its **Domain** and **Client ID**. Set Allowed Callback/Logout/Web
     Origin URLs to `http://localhost:5173`.
   - An **API** for this backend - note its **Identifier** (used as the
     audience). Frontend and backend must reference the exact same identifier
     string.
2. On the API's **Application Access** tab (older Auth0 UI calls this
   "Machine to Machine Applications"), authorize the frontend app's
   **User-delegated Access**. Without this, login succeeds but token requests
   fail with `Client ... is not authorized to access resource server ...`.
3. **MFA**: Security -> Multi-factor Auth -> enable **Email**. Auth0 won't let
   Email be the only enabled factor (a platform restriction, labelled
   "Enterprise MFA" in the dashboard) - enable **One-time Password (OTP)**
   alongside it to satisfy that requirement, then set the enforcement policy
   to **Always** (not Adaptive) so it triggers on every login.
4. **Restricting signups to a whitelist**: the dashboard's **"Disable Sign
   Ups"** toggle (Authentication -> Database -> your connection) only hides
   the Sign Up button from the Universal Login UI - it does **not** block the
   underlying signup endpoint, and does **not** apply to social connections at
   all. Real enforcement needs both of:
   - A **Pre User Registration Action** (Actions -> Library -> Build Custom,
     trigger: Pre User Registration) that checks `event.user.email` against an
     allow-list and calls `api.access.deny(...)` for anyone not on it - wired
     into Actions -> Triggers -> Pre User Registration (dropped into the flow
     diagram, not just deployed) and Applied.
   - **Social connections (e.g. Google) disabled for this application**
     (Application -> Connections tab). A federated social login
     auto-provisions a new Auth0 user on first sign-in and never passes
     through the Pre User Registration trigger - so it bypasses the whitelist
     Action entirely if left enabled. (Found this the hard way: a "new" Google
     account signed in successfully despite the Action being deployed and
     wired in, because it went through `google-oauth2`, not the database
     connection the Action guards.)
5. Test user (per the assignment): `careers@fidenz.com` / `Pass#fidenz` -
   create this manually under User Management -> Users, since public signup
   is disabled.

## Comfort Index formula

**Inputs**: temperature, humidity, wind speed, cloudiness (all directly
available from OpenWeatherMap's current-weather response; dew point was
excluded since the assignment notes it isn't directly available).

**Approach**: each factor produces a "penalty" between 0 (no discomfort) and 1
(maximum discomfort). Humidity and wind are deliberately *not* independent of
temperature - in real weather perception, 90% humidity at 10°C is barely
noticeable, but the same humidity at 35°C is miserable. So those two factors'
penalty functions branch on the current temperature rather than using a fixed
formula regardless of context.

```
score = 100 - 100 * (0.45*tempPenalty + 0.25*humidityPenalty + 0.20*windPenalty + 0.10*cloudPenalty)
```

- **Temperature (weight 0.45)**: zero penalty inside an 18-24°C "ideal band";
  grows linearly outside it, maxing out at ±15°C from the band edge. Given the
  heaviest weight because it's the single biggest driver of perceived comfort.
- **Humidity (weight 0.25)**: only ramps up meaningfully above 20°C (the
  "mugginess" effect); below that it has a much smaller damp-chill effect
  above 80%. This avoids penalizing cool-and-humid mornings as harshly as
  hot-and-humid afternoons.
- **Wind (weight 0.20)**: penalized when cold (wind chill), treated as a small
  *bonus* when hot (cooling breeze), and only lightly penalized at moderate
  temperatures if unusually strong. The one factor that can reduce the total
  penalty rather than add to it.
- **Cloudiness (weight 0.10)**: smallest weight - a minor modifier, not a
  primary driver. Penalized mildly at both extremes (harsh full sun vs. gloomy
  full overcast), treating ~50% cloud cover as neutral.

Score buckets: 80-100 Very Comfortable, 60-79 Comfortable, 40-59 Moderate, <40
Uncomfortable.

### Trade-offs considered

- **Branching penalty functions vs. a single continuous multi-variable
  formula** (e.g. a proper heat-index/wind-chill blend): branching is easier
  to reason about, explain, and unit test, at the cost of small
  discontinuities at the branch boundaries (e.g. exactly 20°C or 26°C).
- **Fixed weights vs. configurable weights**: fixed weights keep the formula
  simple and testable; a production version would likely expose these as
  config so they could be tuned without a redeploy.
- **4 parameters vs. more**: pressure and visibility were left out of v1 to
  keep the formula explainable in a short recording; both are natural
  candidates for a live-added parameter since they affect comfort/safety
  fairly independently of temperature.

## Cache design

Two separate Caffeine caches, both 5-minute TTL, both with stats recording enabled:

- **`weatherRaw`** - keyed by city code, holds the raw OpenWeatherMap response
  per city. Avoids re-hitting OpenWeatherMap's rate-limited API on every
  dashboard refresh.
- **`comfortScores`** - keyed by a constant key (`'all'`), holds the fully
  computed, sorted, ranked list. Separating this from `weatherRaw` means the
  ranked dashboard can be served instantly even if computing/sorting were ever
  more expensive, and the two layers can be reasoned about (or invalidated)
  independently.

`GET /api/debug/cache-status` exposes real hit/miss counters (via Caffeine's
built-in `CacheStats`), plus the actual set of currently-cached keys per cache.

CORS note: Spring Security's filter chain runs before Spring MVC, so CORS
must be wired into Security itself (`http.cors(...)` pointed at a
`CorsConfigurationSource` bean) rather than left as a plain
`WebMvcConfigurer` - otherwise Security blocks the browser's preflight
`OPTIONS` request before CORS headers are ever added.

## Known limitations

- The Comfort Index weights are fixed constants, not empirically tuned
  against real comfort survey data - they encode a reasonable, explainable
  model, not a validated one.
- Cache is in-process (Caffeine), so it won't survive a restart or work
  across multiple backend instances without moving to something like Redis.
- No historical storage - only the current snapshot per city is available.
- Requires a personal Auth0 tenant to run - there's no shared/hosted Auth0
  config, so a reviewer needs to either use provided test credentials against
  the deployed instance, or set up their own tenant per the steps above.


  $env:OWM_API_KEY="4cce64fe586d331eb18b811f69c5f9ca"

  client id   1iRz5xNzipuT6LkQgQURLd2Go8Zq9hTA