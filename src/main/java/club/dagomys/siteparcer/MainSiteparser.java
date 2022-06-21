package club.dagomys.siteparcer;

import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;

@SpringBootApplication
public class MainSiteparser {

    public static void main(String[] args) throws IOException {
        SpringApplication app = new SpringApplication(MainSiteparser.class);
        app.setBannerMode(Banner.Mode.OFF);
        app.run(args);
    }
}
