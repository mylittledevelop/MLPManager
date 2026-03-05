# Integrating MLPManager into MLPInfra

## Summary
Yes, the Dockerfile is correct and you should add MLPManager as a service to your MLPInfra docker-compose.yml.

## How to Integrate

### Step 1: Add MLPManager Service to MLPInfra's docker-compose.yml

In your MLPInfra's `docker-compose.yml`, add this service to the `services:` section (after the `wings:` service):

```yaml
  mlpmanager:
    build:
      context: ../MLPManager  # Relative path to MLPManager directory
      dockerfile: Dockerfile
    container_name: mlpmanager
    restart: unless-stopped
    ports:
      - "8081:8081"
    volumes:
      # Mount host's /etc/pterodactyl for Wings config management
      - /etc/pterodactyl:/etc/pterodactyl:rw
      # Mount Docker socket for Wings container restart
      - /var/run/docker.sock:/var/run/docker.sock:ro
    environment:
      - JAVA_OPTS=-Xmx2G
    depends_on:
      - panel
    networks:
      - ptero-internal
```

### Step 2: Configure MLPManager

You need to make your `config.yml` available to the container. Options:

**Option A: Mount from host (Recommended)**
Add to volumes:
```yaml
      - /path/to/MLPManager/config.yml:/app/config.yml:ro
```

**Option B: Create config via ConfigMap or environment**
If using Kubernetes-like setup, inject config as environment variables.

### Step 3: Build and Run

```bash
cd MLPInfra

# Build all services including MLPManager
docker-compose build

# Start everything
docker-compose up -d

# Verify MLPManager is running
docker-compose ps
docker-compose logs mlpmanager
```

## Dockerfile Analysis

Your current Dockerfile is correct:

✅ **Multi-stage build** - Reduces final image size
✅ **Alpine base** - Lightweight JRE (21)
✅ **Gradle caching** - Dependencies cached separately
✅ **Docker CLI included** - Needed for Wings restart

## Will It Work?

**YES**, with these conditions:

1. ✅ **Wings config path** - `/etc/pterodactyl/config.yml` is shared via volume
2. ✅ **Docker socket access** - Allows MLPManager to restart Wings container
3. ✅ **Network connectivity** - Both on `ptero-internal` network
4. ✅ **Config file** - Mounted into the container
5. ✅ **API Keys** - Set correctly in config.yml

## Startup Flow

1. MLPInfra docker-compose starts
2. Panel comes up first (depends_on)
3. Wings starts (uses host networking)
4. MLPManager starts (depends on panel)
5. MLPManager calls Panel API to create node
6. MLPManager fetches Wings config from Panel
7. MLPManager writes config to `/etc/pterodactyl/config.yml`
8. MLPManager restarts Wings container
9. Wings connects to Panel with proper credentials
10. Node appears as healthy (green) in Panel

## Important Notes

- **Wings container name** must be `wings` (matches your MLPInfra compose)
- **Wings config path** must be `/etc/pterodactyl/config.yml` (already correct in your config.yml)
- **Docker socket** needs read/write access (`:rw` for `/etc/pterodactyl`)
- **depends_on: panel** ensures Panel is ready before MLPManager tries to use it

## Verification Commands

After starting with docker-compose:

```bash
# Check if config was written to host
ls -la /etc/pterodactyl/config.yml

# Check Wings container logs
docker logs wings

# Check MLPManager logs
docker logs mlpmanager

# Verify Wings is now healthy in Panel
# Visit: http://your-server:8090/admin/nodes
```

