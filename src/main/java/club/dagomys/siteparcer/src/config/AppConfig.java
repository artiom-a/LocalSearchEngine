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

    @Bean
    public CommandLineRunner saveSiteToDb(SiteService siteService) throws Exception {
        SiteParser parser = new SiteParser();
        return (String[] args) -> {
            siteList.forEach(site -> {
                if (site.getUrl().endsWith("/")) {
                    site.setUrl(site.getUrl().strip().replaceFirst(".$",""));
                }
                Optional<Site> findSite = siteService.getSite(site.getUrl());
                if (findSite.isEmpty()) {
                    if (site.getName().isEmpty()) {
                        try {
                            Document siteFile = Jsoup
                                    .connect(site.getUrl())
                                    .userAgent(UserAgent)
                                    .referrer("http://www.google.com")
                                    .ignoreHttpErrors(true)
                                    .get();
                            site.setName(siteFile.title());
                        } catch (IOException  e) {
                            site.setLastError("Site is not found");
                            mainLogger.error(site.getUrl() + " " + e.getMessage());
                        }
                        siteService.saveOrUpdate(site);
                    } else {
                        siteService.saveSite(site);
                    }
                } else {
                    siteService.saveOrUpdate(findSite.get());
                }
            });
        };
    }
}
