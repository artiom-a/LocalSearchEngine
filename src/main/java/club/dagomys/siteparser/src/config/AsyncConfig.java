package club.dagomys.siteparser.src.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ForkJoinPoolFactoryBean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.lang.reflect.Method;

@Configuration
@EnableAsync
@Slf4j
public class AsyncConfig implements AsyncConfigurer {

    private final int CORE_COUNT = Runtime.getRuntime().availableProcessors();

    @Bean(name = "taskExecutor")
    @Primary
    public ThreadPoolTaskExecutor taskExecutor() {
        log.info("Creating task executor...");
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
                log.error("Throwable Exception message   : " + ex.getMessage());
                log.error("Method name : " + method.getName());
                for (Object param : params) {
                    log.error("Parameter value             : " + param);
                }
                log.error("stack Trace ");
                ex.printStackTrace();
            }

        };
    }


    @Bean
    public ForkJoinPoolFactoryBean forkJoinPoolFactoryBean() {
        log.info("ForkJoinPool creating...");
        ForkJoinPoolFactoryBean forkJoinPoolFactoryBean = new ForkJoinPoolFactoryBean();
        forkJoinPoolFactoryBean.setParallelism(CORE_COUNT);
        forkJoinPoolFactoryBean.setAwaitTerminationSeconds(4);
        return forkJoinPoolFactoryBean;
    }

    @Bean (name = "siteParserThreadPool")
    public ThreadPoolTaskExecutor siteParserThreadPool() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(CORE_COUNT);
        executor.setMaxPoolSize(CORE_COUNT);
        executor.setThreadNamePrefix("siteParserRunner-");
        executor.initialize();
        return executor;
    }

    @Override
    public ThreadPoolTaskExecutor getAsyncExecutor() {
        return taskExecutor();
    }
}
