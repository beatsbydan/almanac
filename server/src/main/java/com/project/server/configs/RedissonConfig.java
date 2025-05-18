package com.project.server.configs;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.api.RedissonReactiveClient;
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
        return Redisson.create(config);
    }
    @Bean
    public RedissonReactiveClient redissonReactiveClient(RedissonClient redissonClient){
        return redissonClient().reactive();
    }
}
