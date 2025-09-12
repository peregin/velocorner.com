# Use a more specific Node.js version and smaller base image
FROM node:22-alpine AS builder

# Install only necessary system packages and clean up in same layer
RUN apk --no-cache add --virtual .build-deps \
    && apk --no-cache upgrade \
    && apk del .build-deps

WORKDIR /app

# Copy package files for better layer caching
COPY package*.json ./

# Install dependencies with optimizations
RUN npm ci --only=production --no-audit --no-fund \
    && npm install --no-audit --no-fund vite \
    && npm cache clean --force

# Copy source files
COPY src/ ./src/
COPY public/ ./public/
COPY index.html vite.config.js tsconfig.json ./

# Build the application
RUN npm run build \
    && rm -rf node_modules \
    && rm -rf src

# Production stage with minimal Caddy image
FROM caddy:2.9-alpine

# Security updates in single layer
RUN apk --no-cache upgrade \
    && addgroup -g 1001 -S nodejs \
    && adduser -S caddy -u 1001 -G nodejs \
    && mkdir -p /config/caddy /data/caddy \
    && chown -R caddy:nodejs /config/caddy /data/caddy

# Set production environment
ENV NODE_ENV=production

# Copy built assets and configuration
COPY --from=builder --chown=caddy:nodejs /app/dist /usr/share/caddy/html
COPY --chown=caddy:nodejs Caddyfile.live /etc/Caddyfile

# Use non-root user
USER caddy

# Expose port
EXPOSE 3000

# Health check
#HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
#    CMD wget --no-verbose --tries=1 --spider http://localhost:3000/ || exit 1

# Start Caddy
CMD ["caddy", "run", "--config", "/etc/Caddyfile"]
