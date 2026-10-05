package com.skyfare.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

@Configuration
public class AsyncConfig {
    public static final int SEARCH_POOL_SIZE = 8;

    @Bean(destroyMethod = "shutdown")
    public ExecutorService searchExecutor() {
        AtomicInteger counter = new AtomicInteger();
        ThreadFactory factory = r -> {
            Thread t = new Thread(r, "search-" + counter.incrementAndGet());
            t.setDaemon(true);
            return t;
        };
        return Executors.newFixedThreadPool(SEARCH_POOL_SIZE, factory);
    }
}
