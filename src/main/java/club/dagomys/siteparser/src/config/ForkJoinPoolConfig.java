package club.dagomys.siteparser.src.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ForkJoinPoolFactoryBean;

@Configuration
@Slf4j
public class ForkJoinPoolConfig {
    private final int CORE_COUNT = Runtime.getRuntime().availableProcessors();


    @Bean
    public ForkJoinPoolFactoryBean forkJoinPoolFactoryBean() {
        log.info("ForkJoinPool creating...");
        ForkJoinPoolFactoryBean forkJoinPoolFactoryBean = new ForkJoinPoolFactoryBean();
        forkJoinPoolFactoryBean.setParallelism(CORE_COUNT);
        forkJoinPoolFactoryBean.setAwaitTerminationSeconds(4);
        return forkJoinPoolFactoryBean;
    }


}
