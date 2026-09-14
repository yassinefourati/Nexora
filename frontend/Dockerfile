# Build stage
FROM node:22-alpine AS builder
WORKDIR /app
COPY package*.json ./
RUN npm ci --legacy-peer-deps
COPY . .
# Build-time configuration: passed from docker-compose build args (never from a
# developer's local .env, which .dockerignore excludes).
ARG VITE_API_URL=/api/v1
ARG VITE_OIDC_AUTHORITY=http://localhost:9090/realms/fourati-realm
ARG VITE_OIDC_CLIENT_ID=admin-frontend
ARG VITE_OIDC_REDIRECT_URI=http://localhost:5173/auth/callback
ARG VITE_OIDC_POST_LOGOUT_REDIRECT_URI=http://localhost:5173/login
ENV VITE_API_URL=$VITE_API_URL \
    VITE_OIDC_AUTHORITY=$VITE_OIDC_AUTHORITY \
    VITE_OIDC_CLIENT_ID=$VITE_OIDC_CLIENT_ID \
    VITE_OIDC_REDIRECT_URI=$VITE_OIDC_REDIRECT_URI \
    VITE_OIDC_POST_LOGOUT_REDIRECT_URI=$VITE_OIDC_POST_LOGOUT_REDIRECT_URI
RUN npm run build

# Production stage
# Unprivileged image: nginx runs as uid 101 and listens on 8080, no root in the container.
FROM nginxinc/nginx-unprivileged:alpine AS production
# Owned by the unprivileged nginx user so the runtime-config hook can write /config.js
USER root
COPY --from=builder --chown=nginx:nginx /app/dist /usr/share/nginx/html
RUN chown nginx:nginx /usr/share/nginx/html
USER nginx
COPY nginx.conf /etc/nginx/conf.d/default.conf
# Runtime configuration: set APP_API_URL, APP_OIDC_AUTHORITY, APP_OIDC_CLIENT_ID,
# APP_OIDC_REDIRECT_URI, APP_OIDC_POST_LOGOUT_REDIRECT_URI on the container to
# override the build-time values without rebuilding (written to /config.js at start).
COPY --chmod=755 docker/40-runtime-config.sh /docker-entrypoint.d/40-runtime-config.sh
EXPOSE 8080
CMD ["nginx", "-g", "daemon off;"]
