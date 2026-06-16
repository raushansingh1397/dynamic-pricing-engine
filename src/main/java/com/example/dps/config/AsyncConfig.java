package com.example.dps.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;


@Configuration
@EnableAsync
public class AsyncConfig {

    @Value("${spring.task.execution.pool.core-size:10}")
    private int POOL_SIZE;
    @Value("${spring.task.execution.pool.max-size:20}")
    private int MAX_POOL_SIZE;
    @Value("${spring.task.execution.pool.queue-capacity:50}")
    private int QUEUE_CAPACITY;
    @Value("${spring.task.execution.thread-name-prefix:AsyncExecutor-}")
    private String THREAD_NAME_PREFIX;

    @Bean(name = "taskExecutor")
    public Executor taskExecutor(){
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(POOL_SIZE);
        executor.setMaxPoolSize(MAX_POOL_SIZE);
        executor.setQueueCapacity(QUEUE_CAPACITY);
        executor.setThreadNamePrefix(THREAD_NAME_PREFIX);
        executor.initialize();
        return executor;
    }
}
