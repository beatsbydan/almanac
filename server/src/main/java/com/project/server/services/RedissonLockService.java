package com.project.server.services;

import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedissonLockService {
    private final RedissonClient redissonClient;

    public boolean tryLock(String key, long waitTime, long leaseTime){
        RLock lock = redissonClient.getLock(key);
        try{
            return lock.tryLock(waitTime, leaseTime, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    public void unlock(String key){
        RLock lock = redissonClient.getLock(key);

        if(lock.isHeldByCurrentThread()) lock.unlock();
    }
}
