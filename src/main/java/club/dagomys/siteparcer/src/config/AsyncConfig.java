package club.dagomys.siteparcer.src.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {

    private Logger mainLogger = LogManager.getLogger(AsyncConfig.class);
    private final int CORE_COUNT = Runtime.getRuntime().availableProcessors();

    @Bean(name = "taskExecutor")
    public Executor taskExecutor(){
        mainLogger.info("Creating task executor...");
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(CORE_COUNT);
        executor.setThreadNamePrefix("parserThread-");
        executor.initialize();
        return executor;
    }
}
