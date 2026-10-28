package com.project.server.configs;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {
    @Value("${project.redis.address}")
    private String redisAddress;
    @Bean
    public RedissonClient redissonClient(){
        Config config = new Config();
        config.useSingleServer()
                .setAddress(redisAddress)
                .setTimeout(60000);
        // Redisson's defaults (threads=16, nettyThreads=32) are sized for a heavy production
        // Redis cluster with many concurrent connections — found via a threaddump to be the
        // single largest thread consumer in the whole JVM at idle (44 of 81 live threads), wildly
        // oversized for what this app actually does with Redis (schedule-creation locks, one OTP
        // attempt counter). Scaled to the CPU core count instead of accepting the fixed default.
        config.setNettyThreads(Math.max(4, Runtime.getRuntime().availableProcessors() * 2));
        config.setThreads(Math.max(2, Runtime.getRuntime().availableProcessors()));
        return Redisson.create(config);
    }
}
