# Backend for Frontend (BFF)

A thin Node.js/Express layer that sits between the React app and backend APIs.

## Responsibilities
- **Auth cookies**: Sets `httpOnly` refresh token cookie — React app never touches the token
- **CSRF**: Issues and validates CSRF tokens
- **Rate limiting**: Protects login/signup endpoints
- **Response shaping**: Strips internal fields before sending to browser
- **Tenant routing**: Injects `X-Tenant-ID` based on session (not client-provided)

## Structure
```
bff/
├── src/
│   ├── middleware/
│   │   ├── auth.ts        # Validate access token, attach user to req
│   │   ├── csrf.ts        # CSRF token issuance and validation
│   │   ├── rateLimit.ts   # express-rate-limit on auth routes
│   │   └── tenant.ts      # Resolve tenant from subdomain/session
│   ├── routes/
│   │   ├── auth.ts        # POST /auth/login → set httpOnly cookie
│   │   └── proxy.ts       # Proxy all other routes to backend
│   └── index.ts
├── package.json
└── tsconfig.json
```

## When to add this
When deploying to production with real httpOnly cookies. In dev, MSW handles
everything and the BFF is not needed. See `src/core/api/client.ts` for how
the frontend is already prepared to work with a BFF (withCredentials: true).
