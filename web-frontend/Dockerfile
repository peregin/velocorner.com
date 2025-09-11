FROM node:22-alpine AS builder
RUN apk --no-cache upgrade

RUN mkdir /app
WORKDIR /app

# Optimize layer caching by copying package files first
COPY *.json ./
RUN npm install --omit=dev
RUN npm install vite

# Copy only necessary files
COPY /src /app/src
COPY /public /app/public
COPY index.html vite.config.js ./

# build the application
RUN npm run build

# RUN THE APP
FROM caddy:2.9-alpine
RUN apk --no-cache upgrade

# expose PORT 3000 on our virtual machine so we can run our server
EXPOSE 3000

ENV NODE_ENV=production

COPY --from=builder /app/dist /usr/share/caddy/html
COPY Caddyfile.live /etc/Caddyfile
CMD ["caddy", "run", "--config", "/etc/Caddyfile"]