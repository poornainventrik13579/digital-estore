# Certificate Authentication Flow Documentation

## Table of Contents
1. [Architecture Overview](#architecture-overview)
2. [Login Flow](#login-flow)
3. [First Visit Flow](#first-visit-flow)
4. [Page Refresh/Return Visit Flow](#page-refreshreturn-visit-flow)
5. [New Tab Creation Flow](#new-tab-creation-flow)
6. [Session Key Rotation Flow](#session-key-rotation-flow)
7. [Challenge-Response Protocol](#challenge-response-protocol)
8. [Logout Flow](#logout-flow)
9. [Master Key Expiry](#master-key-expiry)
10. [API Endpoints Reference](#api-endpoints-reference)
11. [Security Considerations](#security-considerations)

---

## Architecture Overview

The certificate authentication system uses a **two-tier cryptographic architecture**:

### Tier 1: Master Key Pair
- **Algorithm**: ECDSA P-256
- **Lifetime**: 180 days
- **Storage**:
  - **Private Key**: AES-256-GCM encrypted with PBKDF2, stored in IndexedDB
  - **Public Key**: Stored in MySQL database (`user_certificates` table)
- **Purpose**: Long-term identity, session key signing

### Tier 2: Session Key Pair
- **Algorithm**: ECDSA P-256 (non-extractable)
- **Lifetime**: 4 hours (auto-rotates)
- **Storage**: Memory-only (per tab), Redis server (public keys)
- **Purpose**: Challenge-response authentication for API requests

### Data Storage Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                         Browser (Client)                         │
├─────────────────────────────────────────────────────────────────┤
│  IndexedDB                        Memory (per tab)              │
│  ┌─────────────┐                 ┌─────────────┐               │
│  │ Master Key  │                 │ Session Key │               │
│  │ (encrypted) │                 │ (ephemeral) │               │
│  └─────────────┘                 └─────────────┘               │
└─────────────────────────────────────────────────────────────────┘
                                  │
                                  │ HTTPS + Signature Headers
                                  ▼
┌─────────────────────────────────────────────────────────────────┐
│                         Server                                   │
├─────────────────────────────────────────────────────────────────┤
│  Redis (Cache)                    MySQL (Persistent)            │
│  ┌─────────────┐                 ┌─────────────┐               │
│  │ Challenges  │                 │Master PubKey │               │
│  │ Session Keys│                 │  (user_certificates)        │
│  │ Sessions    │                 │Session Data  │               │
│  │ (cached)    │                 │  (permanent) │               │
│  └─────────────┘                 └─────────────┘               │
│       │                                    │                    │
│       └──────────── Fallback ──────────────┘                    │
│          (MySQL restores Redis sessions)                        │
└─────────────────────────────────────────────────────────────────┘
```

---

## Login Flow

The login flow decides between OAuth2/JWT and Certificate authentication based on the `private_device` flag.

```
                    User Login Request
                           │
                           ▼
              ┌────────────────────────┐
              │  Check private_device  │
              └────────────────────────┘
                    │           │
           private_device:     private_device:
              false              true
                    │           │
                    ▼           ▼
        ┌──────────────┐  ┌──────────────┐
        │ OAuth2/JWT   │  │ Certificate  │
        │ Auth Flow    │  │ Auth Flow    │
        └──────────────┘  └──────────────┘
                │                  │
                ▼                  ▼
        ┌──────────────┐  ┌──────────────┐
        │ Return JWT   │  │ Create       │
        │ Token        │  │ Session, Set │
        │ (1 hour TTL) │  │ certSessionId│
        └──────────────┘  └──────────────┘
```

### Login Endpoints

**Platform Admin Login:**
```
POST /api/v1/auth/platform/login
```

**Tenant Admin Login:**
```
POST /api/v1/auth/tenant/login
```

**User Login:**
```
POST /api/v1/auth/login
```

### Request Body (when `private_device: true`)

```json
{
  "username": "john_doe",
  "password": "securepassword",
  "private_device": true,
  "tenantId": 1
}
```

### Response (Certificate Auth)

```http
HTTP/1.1 200 OK
Set-Cookie: certSessionId=<uuid>; HttpOnly; Path=/; Max-Age=2592000; SameSite=Lax

{
  "message": "Login successful",
  "userId": "user_123"
}
```

---

## First Visit Flow

When a user logs in on a private device for the first time (no master key exists):

```
┌────────────────────────────────────────────────────────────────────┐
│  1. USER LOGIN (private_device: true)                             │
├────────────────────────────────────────────────────────────────────┤
│  POST /api/v1/auth/login (or /tenant/login, /platform/login)      │
│  Request: { username, password, private_device: true }             │
│  Response: Set-Cookie: certSessionId=<uuid>                       │
└────────────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌────────────────────────────────────────────────────────────────────┐
│  2. CHECK MASTER KEY (Client-side)                                │
├────────────────────────────────────────────────────────────────────┤
│  • Check IndexedDB for existing master key                        │
│  • Not found → Proceed to step 3                                  │
└────────────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌────────────────────────────────────────────────────────────────────┐
│  3. GENERATE MASTER KEY PAIR (Client-side)                        │
├────────────────────────────────────────────────────────────────────┤
│  • Generate ECDSA P-256 key pair                                  │
│  • Export public key in SPKI format                               │
│  • Private key.                                                   │
└────────────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌────────────────────────────────────────────────────────────────────┐
│  4. ENCRYPT MASTER PRIVATE KEY (Client-side)                      │
├────────────────────────────────────────────────────────────────────┤
│  • Derive encryption key from user password (PBKDF2)              │
│  • Encrypt private key with AES-256-GCM                           │
│  • Generate IV (12 bytes) and authentication tag                  │
└────────────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌────────────────────────────────────────────────────────────────────┐
│  5. STORE ENCRYPTED KEY (Client-side)                             │
├────────────────────────────────────────────────────────────────────┤
│  • Store encrypted private key + IV + auth tag in IndexedDB       │
│  • Key: "masterKeyEncrypted"                                       │
└────────────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌────────────────────────────────────────────────────────────────────┐
│  6. REGISTER MASTER PUBLIC KEY (Server)                           │
├────────────────────────────────────────────────────────────────────┤
│  POST /api/v1/cert-auth/register-key                              │
│  Headers: X-Challenge, X-Signature (from first challenge)         │
│  Request: { publicKey }                                            │
│  Response: { message, sessionId }                                  │
│                                                                   │
│  Server Action:                                                   │
│  • Verify signature using challenge-response                      │
│  • Store public key in MySQL user_certificates table              │
│  • Generate unique sessionId                                      │
└────────────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌────────────────────────────────────────────────────────────────────┐
│  7. GENERATE SESSION KEY PAIR (Client-side)                       │
├────────────────────────────────────────────────────────────────────┤
│  • Generate ephemeral ECDSA P-256 key pair                        │
│  • Non-extractable private key                                    │
│  • Store in memory only (per tab)                                 │
└────────────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌────────────────────────────────────────────────────────────────────┐
│  8. REGISTER SESSION KEY (Server)                                 │
├────────────────────────────────────────────────────────────────────┤
│  POST /api/v1/cert-auth/register-session-key                      │
│  Headers: X-Challenge, X-Signature                                │
│  Request: { sessionPublicKey, masterSignature, expiresAt }        │
│  Response: { message, keyId }                                      │
│                                                                   │
│  Server Action:                                                   │
│  • Verify master signature on session public key                  │
│  • Store session public key in Redis (4 hour TTL)                │
│  • Pattern: session_key:{userId}:{keyId}                          │
│                                                                   │
│  Client Ready for Challenge-Response Auth!                        │
└────────────────────────────────────────────────────────────────────┘
```

---

## Page Refresh/Return Visit Flow

When user returns to the app (master key exists in IndexedDB):

```
┌────────────────────────────────────────────────────────────────────┐
│  1. CHECK EXISTING SESSION (Client-side)                           │
├────────────────────────────────────────────────────────────────────┤
│  • Check for certSessionId cookie                                  │
│  • Cookie exists → Proceed to step 2                              │
└────────────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌────────────────────────────────────────────────────────────────────┐
│  2. CHECK SESSION VALIDITY (Server)                               │
├────────────────────────────────────────────────────────────────────┤
│  GET /api/v1/cert-auth/check-session                              │
│  Headers: Cookie: certSessionId=<uuid>                            │
│  Response: { valid: true/false, userId, tenantId }                │
│                                                                   │
│  Server Action (Multi-Layer Lookup):                             │
│  • 1st: Check Redis (fast path)                                  │
│      ├─── Found → Return session data immediately                 │
│      └─── Not found → Continue to step 2                         │
│  • 2nd: Check MySQL user_certificates table (fallback)           │
│      ├─── Found → Recreate Redis session, return data             │
│      └─── Not found → Invalid session                             │
└────────────────────────────────────────────────────────────────────┘
                            │
                    ┌──────┴──────┐
                    │             │
              Session Valid   Session Invalid
                    │             │
                    ▼             ▼
        ┌──────────────┐  ┌──────────────────┐
        │ Proceed to   │  │ Redirect to Login│
        │ Step 3       │  │                  │
        └──────────────┘  └──────────────────┘
                    │
                    ▼
┌────────────────────────────────────────────────────────────────────┐
│  3. CHECK MASTER KEY (Client-side)                                │
├────────────────────────────────────────────────────────────────────┤
│  • Load encrypted master key from IndexedDB                        │
│  • Master key found → Proceed to step 4                           │
└────────────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌────────────────────────────────────────────────────────────────────┐
│  4. CHECK MASTER KEY EXPIRY (Client-side)                         │
├────────────────────────────────────────────────────────────────────┤
│  • Check master key creation date (stored in IndexedDB)           │
│  • If age > 180 days → Generate new master key                    │
│  • If age <= 180 days → Proceed to step 5                         │
└────────────────────────────────────────────────────────────────────┘
                            │
                    ┌──────┴──────┐
                    │             │
              Key Expired      Key Valid
                    │             │
                    ▼             ▼
        ┌──────────────┐  ┌──────────────┐
        │ Regenerate   │  │ Proceed to   │
        │ Master Key   │  │ Step 5       │
        │ (Go to Step  │  │              │
        │  3-8 of      │  │              │
        │  First Visit)│  │              │
        └──────────────┘  └──────────────┘
                                    │
                                    ▼
┌────────────────────────────────────────────────────────────────────┐
│  5. GENERATE NEW SESSION KEY (Client-side)                        │
├────────────────────────────────────────────────────────────────────┤
│  • Generate new ephemeral ECDSA P-256 key pair                    │
│  • Non-extractable private key                                    │
│  • Store in memory only (replaces old session key)               │
└────────────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌────────────────────────────────────────────────────────────────────┐
│  6. SIGN SESSION KEY WITH MASTER KEY (Client-side)                │
├────────────────────────────────────────────────────────────────────┤
│  • Decrypt master private key from IndexedDB (using password)     │
│  • Sign session public key with master private key                │
│  • Generate signature in P1363 format                             │
└────────────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌────────────────────────────────────────────────────────────────────┐
│  7. REGISTER NEW SESSION KEY (Server)                             │
├────────────────────────────────────────────────────────────────────┤
│  POST /api/v1/cert-auth/register-session-key                      │
│  Headers: X-Challenge, X-Signature                                │
│  Request: { sessionPublicKey, masterSignature, expiresAt }        │
│  Response: { message, keyId }                                      │
│                                                                   │
│  Server Action:                                                   │
│  • Verify master signature using stored master public key         │
│  • Store new session public key in Redis (4 hour TTL)            │
│  • Old session key expires naturally (TTL)                       │
└────────────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌────────────────────────────────────────────────────────────────────┐
│  8. CLIENT READY (Client-side)                                    │
├────────────────────────────────────────────────────────────────────┤
│  • Session ready for challenge-response authentication            │
│  • All API requests now use challenge-response protocol           │
└────────────────────────────────────────────────────────────────────┘
```

---

## New Tab Creation Flow

Multi-tab support - each tab generates its own session key:

```
┌────────────────────────────────────────────────────────────────────┐
│  TAB 1 (Already Authenticated)                                    │
├────────────────────────────────────────────────────────────────────┤
│  • certSessionId cookie shared across tabs                        │
│  • Session Key 1 in memory (Tab 1 only)                           │
│  • Ready for challenge-response                                   │
└────────────────────────────────────────────────────────────────────┘

┌────────────────────────────────────────────────────────────────────┐
│  TAB 2 (Newly Opened)                                             │
├────────────────────────────────────────────────────────────────────┤
│  • certSessionId cookie available (shared from browser)           │
│  • No session key in memory yet                                   │
└────────────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌────────────────────────────────────────────────────────────────────┐
│  1. CHECK SESSION VALIDITY (Tab 2)                                │
├────────────────────────────────────────────────────────────────────┤
│  GET /api/v1/cert-auth/check-session                              │
│  Response: { valid: true, userId, tenantId }                      │
└────────────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌────────────────────────────────────────────────────────────────────┐
│  2. LOAD MASTER KEY (Tab 2 - Client-side)                         │
├────────────────────────────────────────────────────────────────────┤
│  • Load encrypted master key from IndexedDB                       │
│  • Master key exists (from previous registration)                 │
└────────────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌────────────────────────────────────────────────────────────────────┐
│  3. GENERATE TAB 2 SESSION KEY (Tab 2 - Client-side)              │
├────────────────────────────────────────────────────────────────────┤
│  • Generate new ephemeral ECDSA P-256 key pair                    │
│  • Non-extractable private key                                    │
│  • Store in Tab 2 memory only                                     │
└────────────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌────────────────────────────────────────────────────────────────────┐
│  4. SIGN TAB 2 SESSION KEY (Tab 2 - Client-side)                  │
├────────────────────────────────────────────────────────────────────┤
│  • Decrypt master private key from IndexedDB                      │
│  • Sign Tab 2 session public key with master private key          │
└────────────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌────────────────────────────────────────────────────────────────────┐
│  5. REGISTER TAB 2 SESSION KEY (Server)                           │
├────────────────────────────────────────────────────────────────────┤
│  POST /api/v1/cert-auth/register-session-key                      │
│  Request: { sessionPublicKey, masterSignature, expiresAt }        │
│                                                                   │
│  Server Action:                                                   │
│  • Verify master signature                                        │
│  • Store Tab 2 session public key in Redis                       │
│  • Now Redis has both Session Key 1 and Session Key 2            │
│  • Pattern: session_key:{userId}:{keyId1}, session_key:{userId}:{keyId2}
└────────────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌────────────────────────────────────────────────────────────────────┐
│  RESULT: Multi-Tab Authentication                                 │
├────────────────────────────────────────────────────────────────────┤
│  • Tab 1: Uses Session Key 1 (in memory)                          │
│  • Tab 2: Uses Session Key 2 (in memory)                          │
│  • Both tabs share same certSessionId cookie                      │
│  • Both tabs can authenticate independently                      │
│  • Server verifies signature against ANY registered session key  │
└────────────────────────────────────────────────────────────────────┘
```

---

## Session Key Rotation Flow

Session keys automatically rotate every 4 hours for security:

```
┌────────────────────────────────────────────────────────────────────┐
│  CLIENT SIDE (Timer-based, runs every 4 hours)                    │
├────────────────────────────────────────────────────────────────────┤
│                                                                   │
│  1. Check session key age                                         │
│     │                                                              │
│     ├─── Age < 4 hours → Continue using current session key       │
│     │                                                              │
│     └─── Age >= 4 hours → ROTATE                                  │
│                     │                                              │
│                     ▼                                              │
│  2. Generate new session key pair (ECDSA P-256)                   │
│                     │                                              │
│                     ▼                                              │
│  3. Sign new session public key with master private key           │
│                     │                                              │
│                     ▼                                              │
│  4. Call POST /api/v1/cert-auth/register-session-key              │
│     Request: {                                                     │
│       sessionPublicKey: <new key>,                                │
│       masterSignature: <signature>,                                │
│       expiresAt: <current timestamp + 4 hours>                    │
│     }                                                              │
│                     │                                              │
│                     ▼                                              │
│  5. Replace old session key with new one in memory                │
│                     │                                              │
│                     ▼                                              │
│  6. Old session key expires in Redis (TTL)                       │
│                                                                   │
└────────────────────────────────────────────────────────────────────┘

┌────────────────────────────────────────────────────────────────────┐
│  SERVER SIDE (Redis TTL)                                          │
├────────────────────────────────────────────────────────────────────┤
│  • Session keys stored with 4 hour TTL                           │
│  • Redis automatically expires old keys                          │
│  • No manual cleanup required                                    │
│                                                                   │
│  Storage Pattern:                                                 │
│  Key: session_key:{userId}:{keyId}                               │
│  Value: { sessionPublicKey, expiresAt }                          │
│  TTL: 4 hours (14400 seconds)                                     │
└────────────────────────────────────────────────────────────────────┘
```

---

## Challenge-Response Protocol

All certificate-authenticated API requests use challenge-response:

```
┌────────────────────────────────────────────────────────────────────┐
│  CLIENT REQUEST FLOW                                              │
├────────────────────────────────────────────────────────────────────┤
│                                                                   │
│  1. Request Challenge                                             │
│     POST /api/v1/cert-auth/challenge                              │
│     Headers: Cookie: certSessionId=<uuid>                        │
│     Response: { challengeId, challenge, expiresAt }               │
│                                                                   │
│  2. Sign Challenge (Client-side)                                  │
│     • Get challenge string                                        │
│     • Sign using session private key (ECDSA P-256)                │
│     • Export signature in P1363 format                            │
│     • Encode in Base64                                            │
│                                                                   │
│  3. Make Authenticated API Request                                │
│     POST /api/v1/protected/endpoint                               │
│     Headers:                                                      │
│       X-Challenge: <challengeId>                                  │
│       X-Signature: <base64 signature>                             │
│       Cookie: certSessionId=<uuid>                               │
│     Request: { ...data... }                                       │
│                                                                   │
└────────────────────────────────────────────────────────────────────┘

┌────────────────────────────────────────────────────────────────────┐
│  SERVER VERIFICATION FLOW (CertificateSignatureFilter)            │
├────────────────────────────────────────────────────────────────────┤
│                                                                   │
│  1. Extract Headers                                               │
│     • X-Challenge: <challengeId>                                  │
│     • X-Signature: <base64 signature>                             │
│     • certSessionId cookie → Session data (tenantId, userId)     │
│                                                                   │
│  2. Retrieve Challenge from Redis                                 │
│     Key: challenge:{challengeId}                                  │
│     Validate:                                                     │
│       • Challenge exists                                          │
│       • Not expired (TTL: 10 seconds)                            │
│       • Not used before                                           │
│                                                                   │
│  3. Retrieve Session Keys from Redis                              │
│     Pattern: session_key:{userId}:*                               │
│     Returns: All active session keys for user                    │
│                                                                   │
│  4. Verify Signature (try all session keys)                       │
│     For each session public key:                                  │
│       • Import public key (ECDSA P-256)                           │
│       • Convert signature from P1363 to DER format                │
│       • Verify signature using Java security + BouncyCastle      │
│       • If valid → Mark challenge used, proceed to controller    │
│                                                                   │
│  5. Invalid Signature                                             │
│     • Return 401 Unauthorized                                    │
│     • Error: "Invalid signature"                                  │
│                                                                   │
└────────────────────────────────────────────────────────────────────┘
```

### Challenge Lifecycle

```
Challenge Created (Redis)
    │
    │ TTL: 10 seconds
    │
    ├─── Not used in 10s → Auto-expire (Redis TTL)
    │
    └─── Used successfully → Mark as used (deleted)
```

---

## Logout Flow

```
┌────────────────────────────────────────────────────────────────────┐
│  1. USER LOGOUT (Client-side)                                     │
├────────────────────────────────────────────────────────────────────┤
│  • User clicks logout button                                      │
│  • Client calls logout endpoint                                   │
└────────────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌────────────────────────────────────────────────────────────────────┐
│  2. LOGOUT ENDPOINT (Server)                                      │
├────────────────────────────────────────────────────────────────────┤
│  POST /api/v1/cert-auth/logout                                    │
│  Headers: Cookie: certSessionId=<uuid>                           │
│                                                                   │
│  Server Actions:                                                  │
│  • Remove session from Redis                                      │
│  • Optionally: Remove all session keys for user                  │
│  • Clear certSessionId cookie (set Max-Age=0)                    │
│                                                                   │
│  Response: { message: "Logout successful" }                       │
└────────────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌────────────────────────────────────────────────────────────────────┐
│  3. CLIENT CLEANUP (Client-side)                                  │
├────────────────────────────────────────────────────────────────────┤
│  • Clear session key from memory                                  │
│  • Optionally: Clear master key from IndexedDB (user preference)  │
│  • Redirect to login page                                         │
└────────────────────────────────────────────────────────────────────┘
```

---

## Master Key Expiry

Master keys expire after 180 days for security:

```
┌────────────────────────────────────────────────────────────────────┐
│  CLIENT SIDE (Check on page load)                                 │
├────────────────────────────────────────────────────────────────────┤
│                                                                   │
│  1. Load master key metadata from IndexedDB                       │
│     {                                                             │
│       createdAt: <timestamp>,                                     │
│       encryptedKey: <base64>,                                     │
│       iv: <base64>,                                               │
│       authTag: <base64>                                           │
│     }                                                             │
│                                                                   │
│  2. Calculate age: current_time - created_at                      │
│                                                                   │
│  3. If age > 180 days:                                            │
│       ├─── Display warning: "Master key expired. Please regenerate"│
│       ├─── Generate new master key pair                           │
│       ├─── Encrypt new master private key                         │
│       ├─── Store in IndexedDB (replace old)                      │
│       ├─── Call POST /api/v1/cert-auth/register-key              │
│       └─── Re-register all session keys with new master key      │
│                                                                   │
└────────────────────────────────────────────────────────────────────┘
```

---

## API Endpoints Reference

### Certificate Authentication Endpoints

Base URL: `/api/v1/cert-auth`

#### 1. Register Master Public Key
```
POST /api/v1/cert-auth/register-key

Headers:
  X-Challenge: <challengeId>
  X-Signature: <signature>
  Cookie: certSessionId=<uuid>

Request Body:
{
  "publicKey": "base64-encoded SPKI public key"
}

Response (200 OK):
{
  "message": "Master key registered successfully",
  "sessionId": "unique-session-id"
}
```

#### 2. Request Challenge
```
POST /api/v1/cert-auth/challenge

Headers:
  Cookie: certSessionId=<uuid>

Response (200 OK):
{
  "challengeId": "uuid",
  "challenge": "random-challenge-string",
  "expiresAt": 1234567890
}
```

#### 3. Register Session Key
```
POST /api/v1/cert-auth/register-session-key

Headers:
  X-Challenge: <challengeId>
  X-Signature: <signature>
  Cookie: certSessionId=<uuid>

Request Body:
{
  "sessionPublicKey": "base64-encoded session public key",
  "masterSignature": "base64 signature of session key signed by master key",
  "expiresAt": 1234567890
}

Response (200 OK):
{
  "message": "Session key registered successfully",
  "keyId": "unique-key-id"
}
```

#### 4. Check Session
```
GET /api/v1/cert-auth/check-session

Headers:
  Cookie: certSessionId=<uuid>

Response (200 OK - Valid Session):
{
  "valid": true,
  "userId": "user-123",
  "tenantId": 1
}

Response (200 OK - Invalid Session):
{
  "valid": false
}
```

**Session Lookup Flow (Multi-Layer):**

```
┌────────────────────────────────────────────────────────────────────┐
│  1. Client sends certSessionId cookie                              │
└────────────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌────────────────────────────────────────────────────────────────────┐
│  2. Server checks Redis (Fast Path)                                │
├────────────────────────────────────────────────────────────────────┤
│  • Key: cert_session:{certSessionId}                               │
│  • If found → Return session data immediately                      │
│  • If not found → Proceed to step 3                                │
└────────────────────────────────────────────────────────────────────┘
                            │
                    ┌───────┴───────┐
                    │               │
              Found in Redis    Not in Redis
                    │               │
                    ▼               ▼
        ┌───────────────┐   ┌──────────────────┐
        │ Return userId,│   │ Check MySQL      │
        │ tenantId     │   │ user_certificates │
        │ immediately  │   │ table             │
        └───────────────┘   └──────────────────┘
                                    │
                            ┌───────┴───────┐
                            │               │
                      Found in MySQL    Not in MySQL
                            │               │
                            ▼               ▼
                ┌───────────────┐   ┌──────────────┐
                │ Recreate Redis│   │ Return       │
                │ session from  │   │ valid: false │
                │ MySQL data    │   │ (Login       │
                │ Return userId,│   │ required)    │
                │ tenantId     │   └──────────────┘
                └───────────────┘
```

**Benefits of Multi-Layer Lookup:**
- **Performance**: Redis cache provides fast path for active sessions
- **Resilience**: Survives Redis restarts without requiring re-login
- **Seamless UX**: Sessions automatically restored from MySQL
- **Permanent Storage**: MySQL is source of truth for master keys + sessions

#### 5. Logout
```
POST /api/v1/cert-auth/logout

Headers:
  Cookie: certSessionId=<uuid>

Response (200 OK):
{
  "message": "Logout successful"
}
```

### Login Endpoints (All User Types)

#### Platform Admin Login
```
POST /api/v1/auth/platform/login

Request Body (Certificate Auth):
{
  "username": "admin",
  "password": "password",
  "private_device": true
}

Response (200 OK):
Set-Cookie: certSessionId=<uuid>; HttpOnly; Path=/; Max-Age=2592000
{
  "message": "Login successful",
  "userId": "user-123"
}
```

#### Tenant Admin Login
```
POST /api/v1/auth/tenant/login

Request Body (Certificate Auth):
{
  "tenantId": 1,
  "username": "tenant_admin",
  "password": "password",
  "private_device": true
}

Response (200 OK):
Set-Cookie: certSessionId=<uuid>; HttpOnly; Path=/; Max-Age=2592000
{
  "message": "Login successful",
  "userId": "user-456"
}
```

#### User Login
```
POST /api/v1/auth/login

Request Body (Certificate Auth):
{
  "tenantId": 1,
  "username": "john_doe",
  "password": "password",
  "private_device": true
}

Response (200 OK):
Set-Cookie: certSessionId=<uuid>; HttpOnly; Path=/; Max-Age=2592000
{
  "message": "Login successful",
  "userId": "user-789"
}
```

---

## Security Considerations

### 1. Private Device Flag Obscurity
- **Flag name**: `private_device` (not `certAuth` or `certificateAuth`)
- **Purpose**: Hide dual authentication from attackers
- **Security**: Attackers see standard OAuth flow unless they know to check the flag

### 2. Master Key Protection
- **Encryption**: AES-256-GCM with PBKDF2 key derivation
- **Storage**: Encrypted in IndexedDB, never in plaintext
- **Expiry**: 180 days automatic rotation
- **Non-extractable**: Private key cannot be extracted from Web Crypto API

### 3. Session Key Ephemeral Security
- **Memory-only**: Never stored in persistent storage
- **Per-tab**: Each tab has independent session key
- **Non-extractable**: Cannot be extracted from Web Crypto API
- **Auto-rotation**: Every 4 hours
- **TTL**: Server-side expiration in Redis

### 4. Challenge-Response Security
- **One-time use**: Challenges marked as used after verification
- **Short TTL**: 10 seconds prevents replay attacks
- **Signature verification**: ECDSA P-256 ensures authenticity
- **Multi-key support**: Server tries all active session keys

### 5. Session Security
- **HttpOnly cookies**: certSessionId not accessible via JavaScript
- **SameSite=Lax**: Prevents CSRF attacks
- **Multi-layer session lookup**:
  - 1st: Check Redis (fast path, cached)
  - 2nd: Check MySQL user_certificates table (fallback)
  - Auto-recreate Redis session from MySQL if missing
- **Survives Redis restart**: MySQL as permanent session store
- **Survives Redis TTL expiry**: Session restored from database automatically
- **Server validation**: Every request verified via filter

### 6. Multi-Tenancy Isolation
- **Tenant ID in all entities**: Ensures data isolation
- **Session data includes tenantId**: Prevents cross-tenant access
- **Repository queries filter by tenantId**: Database-level isolation

### 7. Signature Format Conversion
- **P1363 to DER**: Web Crypto API uses P1363, Java uses DER
- **BouncyCastle provider**: Handles cryptographic operations
- **Validation**: Signature format verified before cryptographic verification

### 8. Redis Security
- **Role**: Cache layer for performance (not permanent storage)
- **GenericJackson2JsonRedisSerializer**: Proper JSON serialization
- **Static inner classes**: With default constructors for deserialization
- **TTL-based expiration**: Automatic cleanup of challenges and session keys
- **Pattern-based queries**: Efficient key lookup
- **MySQL fallback**: Sessions restored from database if Redis misses

### 9. Database Security
- **Cascade delete**: User certificates deleted when user deleted
- **Unique constraints**: sessionId unique per user
- **Indexes**: Optimized queries on tenantId, userId, sessionId
- **Audit fields**: created_by, updated_by for compliance

### 10. Filter Security
- **URL pattern restriction**: Only `/api/v1/cert-auth/*` endpoints protected
- **Public endpoints**: `/logout` and `/check-session` exempted
- **Challenge validation**: Existence, expiry, and usage checked
- **Signature verification**: Tries all session keys before rejecting

---

## Data Flow Diagrams

### Complete Authentication Data Flow

```
┌─────────────┐                   ┌─────────────┐
│   Browser   │                   │   Server    │
└─────────────┘                   └─────────────┘
      │                                 │
      │ 1. Login (private_device:true)  │
      ├────────────────────────────────►│
      │                                 │ Create session in Redis
      │ 2. certSessionId cookie         │
      │◄────────────────────────────────┤
      │                                 │
      │ 3. Check IndexedDB              │
      │    (No master key)              │
      │                                 │
      │ 4. Generate master key pair     │
      │    (ECDSA P-256)                │
      │                                 │
      │ 5. Encrypt master private key   │
      │    (AES-256-GCM)                │
      │                                 │
      │ 6. Store in IndexedDB           │
      │                                 │
      │ 7. Request challenge            │
      ├────────────────────────────────►│
      │                                 │ Create challenge in Redis
      │ 8. Challenge + challengeId      │
      │◄────────────────────────────────┤ (TTL: 10s)
      │                                 │
      │ 9. Sign challenge (master key)  │
      │                                 │
      │ 10. Register master public key  │
      ├────────────────────────────────►│
      │    Headers: X-Challenge,        │ Verify signature
      │             X-Signature         │ Store in MySQL
      │                                 │
      │ 11. Master key registered       │
      │◄────────────────────────────────┤
      │                                 │
      │ 12. Generate session key pair   │
      │                                 │
      │ 13. Sign session key (master)   │
      │                                 │
      │ 14. Request challenge           │
      ├────────────────────────────────►│
      │ 15. Challenge + challengeId     │
      │◄────────────────────────────────┤
      │                                 │
      │ 16. Register session key        │
      ├────────────────────────────────►│
      │    Headers: X-Challenge,        │ Verify master signature
      │             X-Signature         │ Store in Redis
      │                                 │
      │ 17. Session key registered      │
      │◄────────────────────────────────┤
      │                                 │
      │ READY FOR AUTHENTICATED APIS    │
      │                                 │
      │ 18. Request challenge           │
      ├────────────────────────────────►│
      │ 19. Challenge + challengeId     │
      │◄────────────────────────────────┤
      │                                 │
      │ 20. Sign challenge (session key)│
      │                                 │
      │ 21. API Request                 │
      ├────────────────────────────────►│
      │    Headers: X-Challenge,        │ Verify signature
      │             X-Signature         │ (try all session keys)
      │                                 │ Mark challenge used
      │                                 │
      │ 22. API Response                │
      │◄────────────────────────────────┤
      │                                 │
```

### Session Key Rotation Data Flow

```
┌─────────────┐                   ┌─────────────┐
│   Browser   │                   │   Server    │
└─────────────┘                   └─────────────┘
      │                                 │
      │ Timer: 4 hours elapsed          │
      │                                 │
      │ 1. Generate new session key     │
      │                                 │
      │ 2. Sign with master key         │
      │                                 │
      │ 3. Request challenge            │
      ├────────────────────────────────►│
      │ 4. Challenge + challengeId      │
      │◄────────────────────────────────┤
      │                                 │
      │ 5. Register new session key     │
      ├────────────────────────────────►│
      │    Headers: X-Challenge,        │ Verify master signature
      │             X-Signature         │ Store new key in Redis
      │                                 │ (TTL: 4 hours)
      │                                 │
      │ 6. New key registered           │
      │◄────────────────────────────────┤
      │                                 │
      │ 7. Replace old session key      │
      │    in memory                    │
      │                                 │
      │ Old key expires in Redis        │
      │ (automatic TTL)                 │
      │                                 │
```

### Multi-Tab Data Flow

```
┌─────────────┐                   ┌─────────────┐
│   TAB 1     │                   │   Server    │
└─────────────┘                   └─────────────┘
      │                                 │
      │ Already authenticated           │
      │ Session Key 1 in memory         │
      │                                 │

┌─────────────┐                   ┌─────────────┐
│   TAB 2     │                   │             │
└─────────────┘                   │             │
      │                         │             │
      │ 1. Check session        │             │
      ├─────────────────────────►│             │
      │ 2. Session valid         │             │
      │◄─────────────────────────┤             │
      │                         │             │
      │ 3. Generate Session Key 2│             │
      │                         │             │
      │ 4. Sign with master key  │             │
      │                         │             │
      │ 5. Request challenge    │             │
      ├─────────────────────────►│             │
      │ 6. Challenge            │             │
      │◄─────────────────────────┤             │
      │                         │             │
      │ 7. Register Session Key 2│             │
      ├─────────────────────────►│ Store Key 2 │
      │ 8. Registered           │ in Redis    │
      │◄─────────────────────────┤             │
      │                         │             │
      │ Now Redis has:          │             │
      │ - Session Key 1         │             │
      │ - Session Key 2         │             │
      │                         │             │
      │ TAB 1 API Request ──────►│ Verify with │
      │ Signature: Key 1         │ Key 1 ✓     │
      │                         │             │
      │ TAB 2 API Request ──────►│ Verify with │
      │ Signature: Key 2         │ Key 2 ✓     │
      │                         │             │
```

---

## Summary

The certificate authentication system provides:

1. **Dual Authentication**: OAuth2/JWT (public device) + Certificate (private device)
2. **Two-Tier Crypto**: Master keys (long-term) + Session keys (ephemeral)
3. **Multi-Tab Support**: Independent session keys per tab
4. **Auto-Rotation**: Session keys rotate every 4 hours
5. **Challenge-Response**: One-time challenges for API security
6. **Obscurity**: `private_device` flag hides dual authentication
7. **Multi-Tenancy**: Full tenant isolation
8. **Production-Ready**: DB + Redis storage, proper TTLs, cleanup
9. **Resilient Sessions**: MySQL as permanent session store with Redis cache + automatic fallback

The system is designed to be secure, maintainable, and scalable while providing a seamless user experience.
