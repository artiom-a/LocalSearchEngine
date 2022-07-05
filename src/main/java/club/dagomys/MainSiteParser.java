package club.dagomys;

import club.dagomys.siteparcer.src.entity.Field;
import club.dagomys.siteparcer.src.services.FieldService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;

@SpringBootApplication
public class MainSiteParser {

    public static void main(String[] args) throws IOException {

        SpringApplication app = new SpringApplication(MainSiteParser.class);
        app.setBannerMode(Banner.Mode.OFF);
        app.run(args);
    }
}
