package club.dagomys.siteparcer.src.config;

import club.dagomys.siteparcer.src.entity.Site;
import lombok.Data;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties("app-configuration")
@Data
public class AppConfig {
    private Logger mainLogger = LogManager.getLogger(AppConfig.class);
    private List<Site> siteList;
    private String UserAgent;

}
