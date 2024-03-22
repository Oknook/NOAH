package com.noah.backend.global.config;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@EnableCaching
@EnableRedisRepositories(basePackages = "com.sample.security.jwt.repository")
public class RedisConfig {

    @Value("${spring.data.redis.host}")
    private String host;

    @Value("${spring.data.redis.port}")
    private int port;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory(host, port);
    }

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        return RedisCacheManager.builder(connectionFactory)
                                .cacheDefaults(defaultCacheConfig())
                                .withInitialCacheConfigurations(getRedisCacheConfigurationMap())
                                .build();
    }

    // 기본 캐시 구성 (TTL: 1시간)
    private RedisCacheConfiguration defaultCacheConfig() {
        return RedisCacheConfiguration.defaultCacheConfig()
                                      .serializeKeysWith(
                                          RedisSerializationContext.SerializationPair.fromSerializer(
                                              new StringRedisSerializer()))
                                      .serializeValuesWith(
                                          RedisSerializationContext.SerializationPair.fromSerializer(
                                              new GenericJackson2JsonRedisSerializer()))
                                      .entryTtl(Duration.ofHours(1));
    }

    private RedisCacheConfiguration rollingPaperCacheConfig() {
        return RedisCacheConfiguration.defaultCacheConfig()
                                      .serializeKeysWith(
                                          RedisSerializationContext.SerializationPair.fromSerializer(
                                              new StringRedisSerializer()))
                                      .serializeValuesWith(
                                          RedisSerializationContext.SerializationPair.fromSerializer(
                                              new GenericJackson2JsonRedisSerializer()))
                                      .entryTtl(Duration.ofMinutes(5));
    }

    private Map<String, RedisCacheConfiguration> getRedisCacheConfigurationMap() {
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        cacheConfigurations.put("rollingPaper", rollingPaperCacheConfig());
        return cacheConfigurations;
    }
}