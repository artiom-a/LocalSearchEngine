package club.dagomys.siteparcer.src.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ForkJoinPoolFactoryBean;

@Configuration
public class ForkJoinPoolConfig {
    private final Logger mainLogger = LogManager.getLogger(ForkJoinPoolConfig.class);
    private final int CORE_COUNT = Runtime.getRuntime().availableProcessors();


    @Bean
    public ForkJoinPoolFactoryBean forkJoinPoolFactoryBean() {
        mainLogger.info("ForkJoinPool creating...");
        ForkJoinPoolFactoryBean forkJoinPoolFactoryBean = new ForkJoinPoolFactoryBean();
        forkJoinPoolFactoryBean.setParallelism(CORE_COUNT);
        forkJoinPoolFactoryBean.setAwaitTerminationSeconds(10);
        return forkJoinPoolFactoryBean;
    }


}
