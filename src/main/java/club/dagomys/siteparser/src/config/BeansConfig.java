package club.dagomys.siteparser.src.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
@ComponentScan(basePackages = {"club.dagomys.siteparser.src.utils.siteparser"})
public class BeansConfig {
/*    @Bean
    public SiteParserRunner createParserTask(Site site) {
        log.info("SITE PARSER CREATED");
        return new SiteParserRunner(site);
    }*/
}
