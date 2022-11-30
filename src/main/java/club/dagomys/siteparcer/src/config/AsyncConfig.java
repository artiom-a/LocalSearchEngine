package club.dagomys.siteparcer.src.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.lang.reflect.Method;
import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

    private Logger mainLogger = LogManager.getLogger(AsyncConfig.class);
    private final int CORE_COUNT = Runtime.getRuntime().availableProcessors();

    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        mainLogger.info("Creating task executor...");
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(CORE_COUNT);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("parserThread-");
        executor.initialize();
        return executor;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new AsyncUncaughtExceptionHandler() {
            @Override
            public void handleUncaughtException(Throwable ex, Method method, Object... params) {
                mainLogger.error("Throwable Exception message   : " + ex.getMessage());
                mainLogger.error("Method name : " + method.getName());
                for (Object param : params) {
                    mainLogger.error("Parameter value             : " + param);
                }
                mainLogger.error("stack Trace ");
                ex.printStackTrace();
            }

        };
    }
}
