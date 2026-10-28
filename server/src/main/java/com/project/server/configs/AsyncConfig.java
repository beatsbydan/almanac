package com.project.server.configs;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.lang.reflect.Method;
import java.util.concurrent.Executor;

/**
 * Backs @Async methods (mail sending — see MailingMgtService) with a bounded named pool instead
 * of Spring's default SimpleAsyncTaskExecutor, which spins up an unbounded new thread per task.
 */
@Configuration
@EnableAsync
@Slf4j
public class AsyncConfig implements AsyncConfigurer {
    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("ProjectAsync-");
        executor.initialize();
        return executor;
    }

    /**
     * @Async void methods can't propagate an exception to any caller — without this, one thrown
     * from a future method that (unlike MailingMgtService.sendMail today) forgets its own
     * try/catch would vanish with no trace at all.
     */
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (Throwable throwable, Method method, Object... params) ->
                log.error("Uncaught exception in async method '{}': {}", method.getName(), throwable.getMessage(), throwable);
    }
}
