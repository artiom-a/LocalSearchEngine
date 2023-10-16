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

@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

    private final Logger mainLogger = LogManager.getLogger(AsyncConfig.class);
    private final int CORE_COUNT = Runtime.getRuntime().availableProcessors();

    @Bean(name = "taskExecutor")
    public ThreadPoolTaskExecutor taskExecutor() {
        mainLogger.info("Creating task executor...");
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(CORE_COUNT);
        executor.setMaxPoolSize(CORE_COUNT);
        executor.setThreadNamePrefix("SiteExecutor-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
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

    @Override
    public ThreadPoolTaskExecutor getAsyncExecutor() {
        return taskExecutor();
    }
}
