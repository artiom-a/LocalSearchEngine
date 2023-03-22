package club.dagomys.siteparcer.src.config;

import club.dagomys.siteparcer.src.entity.Site;
import club.dagomys.siteparcer.src.services.SiteService;
import lombok.Data;
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
    private List<Site> site;

    @Bean
    public CommandLineRunner saveSiteToDb(SiteService siteService) throws Exception {
        return (String[] args) -> {
            site.forEach(site1 -> {
                if (site1.getUrl().endsWith("/")) {
                    site1.setUrl(site1.getUrl().strip().replaceFirst(".$",""));
                }
                Optional<Site> findSite = siteService.getSite(site1.getUrl());
                if (findSite.isEmpty()) {
                    if (site1.getName().isEmpty()) {
                        try {
                            Document siteFile = Jsoup
                                    .connect(site1.getUrl())
                                    .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                                    .referrer("http://www.google.com")
                                    .ignoreHttpErrors(true)
                                    .get();
                            site1.setName(siteFile.title());
                            siteService.saveOrUpdate(site1);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        siteService.saveSite(site1);
                    }
                } else {
                    siteService.saveOrUpdate(findSite.get());
                }
            });
        };
    }
}
