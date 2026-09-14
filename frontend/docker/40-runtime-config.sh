#!/bin/sh
# nginx entrypoint hook (docker-entrypoint.d): writes /config.js from the
# container's environment so one built image can be promoted across
# environments. Only variables that are set override the values baked at build
# time (frontend/src/core/config/env.ts reads window.__APP_CONFIG__ first).
set -eu

OUT=/usr/share/nginx/html/config.js
[ -w "$OUT" ] || [ -w "$(dirname "$OUT")" ] || { echo "runtime config: cannot write $OUT (check image ownership)" >&2; exit 1; }
{
  echo "// generated at container start; do not edit"
  echo "window.__APP_CONFIG__ = {"
  # container variable -> key the frontend reads (src/core/config/env.ts)
  for pair in APP_API_URL:VITE_API_URL APP_OIDC_AUTHORITY:VITE_OIDC_AUTHORITY APP_OIDC_CLIENT_ID:VITE_OIDC_CLIENT_ID               APP_OIDC_REDIRECT_URI:VITE_OIDC_REDIRECT_URI APP_OIDC_POST_LOGOUT_REDIRECT_URI:VITE_OIDC_POST_LOGOUT_REDIRECT_URI               APP_TITLE:VITE_APP_TITLE APP_FLAGS:VITE_FLAGS; do
    var=${pair%%:*}; key=${pair#*:}
    val=$(printenv "$var" 2>/dev/null || true)
    if [ -n "$val" ]; then
      # escape backslashes and double quotes for a JS string literal
      esc=$(printf '%s' "$val" | sed 's/\\/\\\\/g; s/"/\\"/g')
      echo "  \"$key\": \"$esc\","
    fi
  done
  echo "};"
} > "$OUT"
echo "runtime config written to $OUT"
