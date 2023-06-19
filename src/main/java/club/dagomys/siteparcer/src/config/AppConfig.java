package club.dagomys.siteparcer.src.config;

import club.dagomys.siteparcer.src.entity.Site;
import club.dagomys.siteparcer.src.services.SiteParser;
import club.dagomys.siteparcer.src.services.SiteService;
import lombok.Data;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Configuration
@ConfigurationProperties("app-configuration")
@Data
public class AppConfig {
    private Logger mainLogger = LogManager.getLogger(AppConfig.class);
    private List<Site> siteList;
    private String UserAgent;

}
