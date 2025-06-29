# Velocorner Deployment Guide

This guide explains how to deploy the decoupled Velocorner application, which consists of a React frontend and a Scala Play backend.

## Architecture Overview

The application has been decoupled into two separate components:

1. **Frontend** (`web-frontend/`): React application serving the user interface
2. **Backend** (`web-app/`): Scala Play application serving REST APIs

## Prerequisites

- Java 11+ (for backend)
- Node.js 16+ (for frontend)
- Docker (optional, for containerized deployment)
- PostgreSQL database
- Redis (for caching)

## Backend Deployment

### 1. Build the Backend

```bash
cd web-app
sbt clean compile dist
```

This creates a distribution package in `target/universal/`.

### 2. Configure Environment

Create a configuration file for your environment:

```bash
# conf/application.prod.conf
include "application.conf"

# Database configuration
db.default.driver=org.postgresql.Driver
db.default.url="jdbc:postgresql://localhost:5432/velocorner"
db.default.username="your_db_user"
db.default.password="your_db_password"

# Redis configuration
play.cache.redis.host="localhost"
play.cache.redis.port=6379

# Strava API configuration
strava.client.id="your_strava_client_id"
strava.client.secret="your_strava_client_secret"

# Other service configurations...
```

### 3. Deploy Backend

#### Option A: Direct Deployment

```bash
# Extract the distribution
unzip target/universal/velocorner-web-app-*.zip
cd velocorner-web-app-*

# Start the application
./bin/velocorner-web-app -Dconfig.file=conf/application.prod.conf
```

#### Option B: Docker Deployment

```dockerfile
# Dockerfile.backend
FROM openjdk:11-jre-slim

WORKDIR /app
COPY target/universal/velocorner-web-app-*.zip /app/
RUN unzip *.zip && rm *.zip

EXPOSE 9000
CMD ["./velocorner-web-app-*/bin/velocorner-web-app", "-Dconfig.file=conf/application.prod.conf"]
```

```bash
docker build -f Dockerfile.backend -t velocorner-backend .
docker run -p 9000:9000 velocorner-backend
```

### 4. API-Only Configuration

For the decoupled architecture, use the API-only routes:

```bash
# Copy the API routes
cp conf/routes.api conf/routes

# Or modify the existing routes to remove web page routes
```

## Frontend Deployment

### 1. Build the Frontend

```bash
cd web-frontend
npm install
npm run build
```

This creates a production build in the `build/` directory.

### 2. Configure API Endpoint

Set the backend API endpoint:

```bash
# Create .env.production
REACT_APP_API_HOST=https://api.velocorner.com
```

### 3. Deploy Frontend

#### Option A: Static File Server

```bash
# Using nginx
sudo apt-get install nginx
sudo cp -r build/* /var/www/html/
sudo systemctl restart nginx
```

#### Option B: Docker Deployment

```dockerfile
# Dockerfile.frontend
FROM nginx:alpine

COPY build/ /usr/share/nginx/html/
COPY nginx.conf /etc/nginx/nginx.conf

EXPOSE 80
```

```nginx
# nginx.conf
server {
    listen 80;
    server_name localhost;
    root /usr/share/nginx/html;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }

    location /api/ {
        proxy_pass http://backend:9000;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

```bash
docker build -f Dockerfile.frontend -t velocorner-frontend .
docker run -p 80:80 velocorner-frontend
```

## Production Deployment

### 1. Load Balancer Configuration

Set up a load balancer (e.g., nginx, HAProxy) to route traffic:

```nginx
# nginx.conf
upstream backend {
    server backend1:9000;
    server backend2:9000;
}

upstream frontend {
    server frontend1:80;
    server frontend2:80;
}

server {
    listen 80;
    server_name velocorner.com;

    location / {
        proxy_pass http://frontend;
    }

    location /api/ {
        proxy_pass http://backend;
    }

    location /health {
        proxy_pass http://backend;
    }
}
```

### 2. SSL/TLS Configuration

```bash
# Install Certbot
sudo apt-get install certbot python3-certbot-nginx

# Obtain SSL certificate
sudo certbot --nginx -d velocorner.com -d www.velocorner.com
```

### 3. Environment Variables

Set up environment variables for the backend:

```bash
# /etc/environment
DB_HOST=your_db_host
DB_NAME=velocorner
DB_USER=your_db_user
DB_PASSWORD=your_db_password
REDIS_HOST=your_redis_host
STRAVA_CLIENT_ID=your_strava_client_id
STRAVA_CLIENT_SECRET=your_strava_client_secret
```

## Monitoring and Health Checks

### 1. Health Check Endpoints

- Backend: `GET /health` or `GET /api/ping`
- Frontend: Static file serving

### 2. Logging

Configure logging for both components:

```bash
# Backend logging
tail -f logs/application.log

# Frontend logging (if using nginx)
tail -f /var/log/nginx/access.log
tail -f /var/log/nginx/error.log
```

### 3. Monitoring

Set up monitoring with tools like:
- Prometheus + Grafana
- New Relic
- DataDog

## Database Setup

### 1. PostgreSQL

```sql
-- Create database
CREATE DATABASE velocorner;

-- Create user
CREATE USER velocorner_user WITH PASSWORD 'your_password';
GRANT ALL PRIVILEGES ON DATABASE velocorner TO velocorner_user;
```

### 2. Run Migrations

```bash
# The Play application will run migrations automatically
# or you can run them manually
sbt flywayMigrate
```

## CORS Configuration

Ensure CORS is properly configured for the decoupled architecture:

```scala
// In application.conf
play.filters.cors {
  allowedOrigins = ["https://velocorner.com", "https://www.velocorner.com"]
  allowedHttpMethods = ["GET", "POST", "PUT", "DELETE", "OPTIONS"]
  allowedHttpHeaders = ["Accept", "Content-Type", "Authorization"]
}
```

## Troubleshooting

### Common Issues

1. **CORS Errors**: Check CORS configuration in backend
2. **API Connection**: Verify API_HOST environment variable
3. **Database Connection**: Check database credentials and connectivity
4. **SSL Issues**: Ensure certificates are valid and properly configured

### Debug Commands

```bash
# Check backend status
curl http://localhost:9000/health

# Check frontend
curl http://localhost:80

# Check database connection
psql -h localhost -U velocorner_user -d velocorner

# Check logs
tail -f logs/application.log
```

## Scaling

### Horizontal Scaling

1. **Backend**: Deploy multiple instances behind a load balancer
2. **Frontend**: Deploy to CDN or multiple static file servers
3. **Database**: Consider read replicas for read-heavy workloads

### Performance Optimization

1. **Caching**: Use Redis for session and data caching
2. **CDN**: Serve static assets from CDN
3. **Database**: Optimize queries and add indexes
4. **Compression**: Enable gzip compression

## Security Considerations

1. **HTTPS**: Always use HTTPS in production
2. **API Keys**: Store sensitive configuration securely
3. **Database**: Use strong passwords and limit access
4. **Firewall**: Configure firewall rules appropriately
5. **Updates**: Keep dependencies updated

## Backup Strategy

1. **Database**: Regular PostgreSQL backups
2. **Configuration**: Version control for configuration files
3. **Logs**: Rotate and archive logs
4. **Static Assets**: Backup frontend build artifacts 