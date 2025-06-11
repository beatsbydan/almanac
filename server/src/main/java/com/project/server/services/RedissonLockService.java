package com.project.server.services;

import com.project.server.exceptions.ScheduleUpsertException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
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

    public void unlock(String key) throws ScheduleUpsertException {
        RLock lock = redissonClient.getLock(key);

        if(lock.isHeldByCurrentThread()) {
            try{
                lock.unlock();
            }
            catch(Exception e){
                log.warn(e.getMessage());
                throw new ScheduleUpsertException("Something went wrong, Please try again");
            }
        }
    }
}
