package club.dagomys.siteparcer.src.config;

import club.dagomys.siteparcer.src.entity.Site;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties("app-configuration")
@Data
public class AppConfig {
    private List<Site> siteList;

    private String UserAgent;
    private String referer;

}
