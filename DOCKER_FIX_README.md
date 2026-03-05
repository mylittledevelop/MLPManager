# MLPManager Docker Configuration Fix

## Problem
The application was failing to start in Docker with:
```
NullPointerException: Cannot invoke "gg.mylittleplanet.manager.config.PanelConfig.getBaseUrl()" 
because the return value of "gg.mylittleplanet.manager.config.ManagerConfig.getPanel()" is null
```

## Root Causes

### 1. Missing Configuration File in Docker
The application expects a `config.yml` file at `/app/config.yml`, but it wasn't being mounted into the container.

### 2. Wrong Environment Variables
The Docker Compose was setting environment variables that don't match the application's expected configuration structure:
- ❌ `PTERODACTYL_PANEL_URL` 
- ❌ `PTERODACTYL_API_KEY`
- ✅ Should be in `config.yml` as `manager.panel.base-url` and `manager.panel.app-api-key`

### 3. Wrong Port Mapping
- Docker Compose exposed port `8080`, but the app runs on `8081` (see `application.yml`)

### 4. Wrong Panel URL for Docker Networking
- `config.yml` uses `http://198.50.209.166:8082` (external URL)
- In Docker Compose, services should use service names: `http://panel:80`

## Solution

### Step 1: Update ManagerConfig.java (DONE ✅)
Added `@NestedConfigurationProperty` annotations to ensure Spring Boot properly binds nested configuration properties.

### Step 2: Create Docker-specific Config
Created `config.docker.yml` with the correct internal Docker service URLs:
```yaml
manager:
  panel:
    base-url: "http://panel:80"  # Uses Docker service name
    app-api-key: "ptla_..."
    client-api-key: "ptlc_..."
```

### Step 3: Update Docker Compose

Replace the `mlp-manager` service in your `docker-compose.yml` with:

```yaml
  mlp-manager:
    build:
      context: ./MLPManager
      dockerfile: Dockerfile
    restart: unless-stopped
    networks:
      - ptero-internal
    ports:
      - "8081:8081"  # Fixed: was 8080:8080
    volumes:
      - /var/run/docker.sock:/var/run/docker.sock
      - /var/lib/pterodactyl:/var/lib/pterodactyl
      - /etc/pterodactyl:/etc/pterodactyl
      - ./MLPManager/config.docker.yml:/app/config.yml:ro  # Mount config file
    depends_on:
      panel:
        condition: service_healthy
      mysql:
        condition: service_healthy
```

**Remove these environment variables** - they're not being used:
```yaml
    # DELETE THIS SECTION:
    # environment:
    #   PTERODACTYL_PANEL_URL: "http://panel:80"
    #   PTERODACTYL_API_KEY: ${PTERODACTYL_API_KEY}
    #   SPRING_DATASOURCE_URL: "jdbc:mysql://mysql:3306/panel"
    #   SPRING_DATASOURCE_USERNAME: pterodactyl
    #   SPRING_DATASOURCE_PASSWORD: ${MYSQL_PASSWORD}
    #   SPRING_REDIS_HOST: ptero-redis
    #   SPRING_REDIS_PORT: 6379
```

## How to Apply

1. **Update your `docker-compose.yml`** with the fixed `mlp-manager` service configuration above

2. **Rebuild and restart the container:**
   ```bash
   docker-compose down
   docker-compose build mlp-manager
   docker-compose up -d
   ```

3. **Check the logs:**
   ```bash
   docker-compose logs -f mlp-manager
   ```

You should now see:
```
Initializing PteroAppClient with baseUrl: http://panel:80
API Key configured: true
```

## Notes

- `config.yml` is for local development (uses external IPs)
- `config.docker.yml` is for Docker deployment (uses Docker service names)
- The application will load config from `/app/config.yml` in the container
- Spring Boot automatically converts `base-url` to `baseUrl` (kebab-case to camelCase)

