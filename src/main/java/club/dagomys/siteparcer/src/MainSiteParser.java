package club.dagomys.siteparcer.src;

import club.dagomys.siteparcer.src.entity.Lemma;
import club.dagomys.siteparcer.src.entity.Page;
import club.dagomys.siteparcer.src.entity.SearchIndex;
import club.dagomys.siteparcer.src.entity.Site;
import club.dagomys.siteparcer.src.services.MainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

import java.io.IOException;

@SpringBootApplication
public class MainSiteParser {

    public static void main(String[] args) throws IOException {
        SpringApplication app = new SpringApplication(MainSiteParser.class);
        app.setBannerMode(Banner.Mode.OFF);
        app.setLazyInitialization(true);
        app.run(args);
    }
}
