I’ll pull the exact cache configuration and a few representative annotations so the guide can point to concrete places in the project rather than staying abstract.
I have the relevant lines now. I’ll turn that into a project-specific guide: what Redis is doing here, how requests flow through the cache, how keys and invalidation work, and how to debug it locally.
Redis Guide Redis is used in this project as a cache layer, not as the main database. PostgreSQL remains the source of truth. Redis stores temporary copies of service responses so repeated reads can avoid hitting PostgreSQL until the cached value expires or is evicted.
Start with these files:
•
pom.xml: adds Spring cache and Redis dependencies.
•
CacheConfig.java: enables caching and configures Redis cache behavior.
•
application.properties: tells Spring to use Redis for cache.
•
docker-compose.yml: runs Redis locally/in Docker.
1. How Redis Is Enabled The dependencies are:
   spring-boot-starter-cache
   spring-boot-starter-data-redis
   spring-boot-starter-cache gives Spring the cache abstraction: @Cacheable, @CacheEvict, @Caching.
   spring-boot-starter-data-redis gives Spring the Redis connection and Redis-backed cache implementation.
   Caching is activated here:
   CacheConfig.java
   @EnableCaching
   Without @EnableCaching, the annotations in the service layer would do nothing.
2. Redis Connection Settings Default settings:
   application.properties
   spring.cache.type=redis
   spring.data.redis.host=${REDIS_HOST:localhost}
   spring.data.redis.port=${REDIS_PORT:6379}
   spring.data.redis.repositories.enabled=false
   This means:
   •
   Use Redis as the Spring cache backend.
   •
   Use localhost:6379 by default.
   •
   Allow override via REDIS_HOST and REDIS_PORT.
   •
   Disable Redis repository scanning because this project uses JPA repositories, not Redis repositories.
   Local profile also points to localhost:
   application-local.properties
   Production profile defaults to Docker service name redis:
   application-prod.properties
   In Docker, backend gets:
   docker-compose.yml
   REDIS_HOST: redis
   REDIS_PORT: 6379
3. Cache Manager The cache manager is the bridge between Spring cache annotations and Redis.
   CacheConfig.java
   Important settings:
   .entryTtl(Duration.ofMinutes(10))
   .disableCachingNullValues()
   .computePrefixWith(cacheName -> "constructiq:" + cacheName + "::")
   Meaning:
   •
   Most cached values live for 10 minutes.
   •
   Null values are not cached.
   •
   Redis keys are prefixed like:
   constructiq:tasks::...
   constructiq:projects::...
   constructiq:dashboardStatistics::...
   Special TTLs:
   •
   Dashboard statistics: 2 minutes.
   •
   Users: 30 minutes.
   Defined here:
   CacheConfig.java
4. What Gets Cached The project caches service-layer read results, mostly DTO responses.
   Examples:
   Project list:
   ProjectService.java
   @Cacheable(cacheNames = CacheConfig.PROJECTS, key = "#authentication.name + ':mine'")
   Task list by project:
   TaskService.java
   @Cacheable(cacheNames = CacheConfig.TASKS, key = "#authentication.name + ':project:' + #projectId")
   Dashboard current statistics:
   DashboardService.java
   @Cacheable(cacheNames = CacheConfig.DASHBOARD_STATISTICS, key = "#authentication.name + ':current'")
   User lookup:
   UserService.java
   @Cacheable(cacheNames = CacheConfig.USERS, key = "'id:' + #userId")
5. How @Cacheable Works When a method with @Cacheable is called:
1.
Spring builds the cache key from the annotation.
2.
Spring checks Redis for that key.
3.
If found, Spring returns the cached value immediately.
4.
If not found, the method runs normally.
5.
The return value is stored in Redis.
6.
Next call with the same key uses Redis.
Example:
getTasksByProject(10, authentication)
For user alice@example.com, the key becomes conceptually:
constructiq:tasks::alice@example.com:project:10
The actual Redis key may include Spring serialization details, but this is the logical shape.
6. Why Keys Include Authentication Name Many service methods perform permission checks.
   For example, getProjectById checks whether the current user can access the project. If the cache key only used project ID, this could happen:
1.
Admin requests project 10.
2.
Project 10 is cached.
3.
Another user requests project 10.
4.
Cache returns the project before access logic runs.
That would be a security bug.
So keys include:
#authentication.name
That makes the cache user-scoped:
alice@example.com:project:10
bob@example.com:project:10
Same project, different users, different cache entries.
7. How Cache Eviction Works Reads are cached. Writes clear affected caches.
   Example: creating a task:
   TaskService.java
   @Caching(evict = {
   @CacheEvict(cacheNames = CacheConfig.TASKS, allEntries = true),
   @CacheEvict(cacheNames = CacheConfig.DASHBOARD_STATISTICS, allEntries = true)
   })
   Meaning:
   •
   A new task changes task lists.
   •
   A new task also changes dashboard counts.
   •
   So both caches are cleared.
   This project mostly uses allEntries = true. That is intentionally conservative. It is less precise, but avoids stale data bugs across user-scoped list/detail caches.
8. Why DTOs Are Serializable Redis stores cached return values. The current cache setup uses Spring’s default Redis cache serialization, so cached objects must be serializable.
   That is why response DTOs implement Serializable, for example:
   TaskResponse.java
   public class TaskResponse implements Serializable
   The service methods cache DTOs, not JPA entities. That is good. Caching entities can cause lazy-loading and session problems.
9. Request Flow Example Take this endpoint flow:
   GET /api/projects/10/tasks
   Controller calls:
   taskService.getTasksByProject(10, authentication)
   Spring sees:
   TaskService.java
   @Cacheable(cacheNames = CacheConfig.TASKS, key = "#authentication.name + ':project:' + #projectId")
   First request:
   •
   Redis miss.
   •
   Service loads current user.
   •
   Service loads project.
   •
   Access check runs.
   •
   Repository queries PostgreSQL.
   •
   DTO list is returned.
   •
   DTO list is saved to Redis.
   Second request from same user:
   •
   Redis hit.
   •
   Method body is skipped.
   •
   PostgreSQL is not queried.
   •
   Cached DTO list is returned.
   After task update/delete/create:
   •
   tasks cache is cleared.
   •
   Next read goes back to PostgreSQL and refreshes Redis.
10. Cache Names In This Project Defined in CacheConfig.java:
    projects
    tasks
    risks
    progressReports
    documents
    registrations
    dashboardStatistics
    users
    Think of each cache name as a Redis namespace.
11. Running Redis Locally With Docker Compose:
    docker compose up redis
    Or run the full stack:
    docker compose up --build
    The Redis service is here:
    docker-compose.yml
    redis:
    image: redis:7
    container_name: constructiq-redis
    ports:
    - "6379:6379"
12. Inspecting Redis Open Redis CLI inside the container:
    docker exec -it constructiq-redis redis-cli
    Useful commands:
    KEYS constructiq:*
    TTL "some-key"
    GET "some-key"
    DEL "some-key"
    FLUSHDB
    Use KEYS constructiq:* only in local development. In production, prefer SCAN.
13. How To Prove Cache Is Working Use the same API twice.
    First request:
    •
    You should see SQL queries in logs.
    Second identical request:
    •
    You should see fewer or no SQL queries for that service method.
    Then mutate data, for example create/update/delete a task:
    •
    Cache is evicted.
    •
    Next read should hit PostgreSQL again.
    •
    The following read should come from Redis.
14. Current Tradeoffs The cache strategy is safe and simple:
    •
    User-scoped keys protect permission-sensitive reads.
    •
    Broad eviction avoids stale data.
    •
    DTO caching avoids JPA entity problems.
    •
    TTLs prevent old data from living forever.
    The tradeoff:
    •
    allEntries = true clears more cache entries than strictly necessary.
    •
    This is acceptable for a small/medium app and much safer than under-evicting.
    A future optimization would be targeted eviction by project ID and user ID, but that requires more careful key tracking.
15. Mental Model Use this rule:
    PostgreSQL owns the truth.
    Redis owns temporary copies.
    @Cacheable reads from Redis.
    @CacheEvict removes stale Redis copies after writes.
    TTL is the backup cleanup mechanism.
    That is the whole Redis story in this project.