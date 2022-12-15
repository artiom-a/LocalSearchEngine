package club.dagomys.siteparcer.src.config;

import club.dagomys.siteparcer.src.entity.Field;
import club.dagomys.siteparcer.src.entity.FieldSelector;
import club.dagomys.siteparcer.src.entity.Site;
import club.dagomys.siteparcer.src.entity.SiteStatus;
import club.dagomys.siteparcer.src.services.FieldService;
import club.dagomys.siteparcer.src.services.SiteService;
import lombok.Data;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Configuration
@ConfigurationProperties("sites")
@Data
public class AppConfig {
    private List<Site> site;

    @Bean
    public CommandLineRunner saveSiteToDb(SiteService siteService) throws Exception {
        return (String[] args) -> {
            site.forEach(s->{
                s.setStatus(SiteStatus.ADDED);
                siteService.saveSite(s);
                    });
        };
    }
}
