package com.constructiq.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import java.time.Duration;
import java.util.Map;

@Configuration
@EnableCaching
public class CacheConfig {

    public static final String PROJECTS = "projects";
    public static final String TASKS = "tasks";
    public static final String RISKS = "risks";
    public static final String PROGRESS_REPORTS = "progressReports";
    public static final String DOCUMENTS = "documents";
    public static final String REGISTRATIONS = "registrations";
    public static final String DASHBOARD_STATISTICS = "dashboardStatistics";
    public static final String USERS = "users";

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .disableCachingNullValues()
                .computePrefixWith(cacheName -> "constructiq:" + cacheName + "::");

        Map<String, RedisCacheConfiguration> cacheConfigurations = Map.of(
                DASHBOARD_STATISTICS, defaultConfig.entryTtl(Duration.ofMinutes(2)),
                USERS, defaultConfig.entryTtl(Duration.ofMinutes(30))
        );

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .transactionAware()
                .build();
    }
}
