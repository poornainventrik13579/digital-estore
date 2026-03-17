# Changes Made & Environment Switching Guide

---

## ALL FILES CHANGED

### BACKEND (Spring Boot)

| File | What Changed |
|------|-------------|
| `src/main/resources/application.properties` | Cookie config: `secure=true`, `same-site=None` for ngrok |
| `src/main/java/.../service/certificate/SessionHelper.java` | Added `cookieSameSite` config property; added `X-Session-ID` header fallback so Safari/Android can auth without cookies |
| `src/main/java/.../api/UserAuthController.java` | Added `sessionId` to cert login response body; updated `@CrossOrigin` to allow ngrok + port 4201 |
| `src/main/java/.../api/CertificateAuthController.java` | Updated `@CrossOrigin` to allow ngrok + port 4201 |
| `src/main/java/.../config/AuthServerConfig.java` | Added ngrok origins + `localhost:4201` to CORS; added `X-Session-ID` + `ngrok-skip-browser-warning` to allowed headers |
| `src/main/java/.../service/certificate/CertificateServiceImpl.java` | `markChallengeUsed` now deletes the Redis key instead of resetting its TTL |

### FRONTEND (Angular)

| File | What Changed |
|------|-------------|
| `src/environments/environment.development.ts` | `apiBaseUrl` changed to ngrok URL |
| `src/app/modules/services/auth.service.ts` | (1) `initializeCrypto` gets `forceRegister` param — fixes re-login after logout; (2) Clears in-memory keys + `certSessionId` on logout; (3) Session key TTL fixed 1h → 4h; (4) `extractable: false` → `extractable: true` for both key pairs (fixes Safari/Android WebCrypto); (5) Clears `certSessionId` from localStorage on logout |
| `src/app/modules/components/login/login.component.ts` | Passes `forceRegister=true` to `initializeCrypto`; stores `certSessionId` from login response; sets `isLoggedIn` earlier |
| `src/app/core/interceptors/auth.interceptor.ts` | Adds `X-Session-ID` + `ngrok-skip-browser-warning` headers to all cert-mode requests via `baseHeaders` |

---

## SWITCHING: NGROK → LOCALHOST

### Step 1 — `application.properties` (backend)

Change:
```properties
app.cookie.secure=true
app.cookie.same-site=None
```
To:
```properties
app.cookie.secure=false
app.cookie.same-site=Lax
```

> **Why:** `Secure=true` requires HTTPS. Localhost runs on HTTP so the browser
> won't send the cookie back. `SameSite=Lax` is fine for localhost since
> both frontend and backend are on the same host.

---

### Step 2 — `environment.development.ts` (frontend)

Change:
```typescript
apiBaseUrl: 'https://XXXX-XXXX.ngrok-free.app/api/v1'
```
To:
```typescript
apiBaseUrl: 'http://localhost:8081/api/v1'
```

---

### Step 3 — Restart backend

```bash
# Stop the running Spring Boot server (Ctrl+C), then:
./mvnw spring-boot:run
```

---

### Step 4 — Rebuild frontend

```bash
ng build --configuration development
# or if using dev server:
ng serve --port 4201
```

---

### Step 5 — Clear browser data

Clear localStorage + cookies in your browser before testing.
Chrome DevTools → Application → Storage → Clear site data.

---

## SWITCHING: LOCALHOST → NGROK

### Step 1 — Start ngrok

```bash
ngrok http 8081
```
Copy the HTTPS forwarding URL (e.g. `https://XXXX.ngrok-free.app`).

---

### Step 2 — `application.properties` (backend)

Change:
```properties
app.cookie.secure=false
app.cookie.same-site=Lax
```
To:
```properties
app.cookie.secure=true
app.cookie.same-site=None
```

---

### Step 3 — `environment.development.ts` (frontend)

Change:
```typescript
apiBaseUrl: 'http://localhost:8081/api/v1'
```
To:
```typescript
apiBaseUrl: 'https://XXXX.ngrok-free.app/api/v1'
```
*(use the URL from Step 1)*

---

### Step 4 — Restart backend + rebuild frontend

```bash
# Backend (Ctrl+C then):
./mvnw spring-boot:run

# Frontend:
ng build --configuration development
# or:
ng serve --port 4201
```

---

### Step 5 — Clear browser data

Clear localStorage + cookies in all browsers before testing (Chrome, Safari, Android).

---

## QUICK REFERENCE

| Setting | Localhost | Ngrok |
|---------|-----------|-------|
| `app.cookie.secure` | `false` | `true` |
| `app.cookie.same-site` | `Lax` | `None` |
| `apiBaseUrl` | `http://localhost:8081/api/v1` | `https://XXXX.ngrok-free.app/api/v1` |
| Backend restart needed? | Yes | Yes |
| Frontend rebuild needed? | Yes | Yes |
| Clear browser data? | Yes | Yes |

> **Note:** Every time you restart ngrok, the URL changes (free plan).
> You must update `environment.development.ts` with the new URL each time.
> To avoid this, use a paid ngrok plan with a fixed domain.
