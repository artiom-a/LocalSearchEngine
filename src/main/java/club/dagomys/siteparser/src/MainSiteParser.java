package club.dagomys.siteparser.src;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;

@SpringBootApplication
@OpenAPIDefinition
public class MainSiteParser{
    public static void main(String[] args) throws IOException {
        SpringApplication app = new SpringApplication(MainSiteParser.class);
        app.setBannerMode(Banner.Mode.OFF);
        app.setLazyInitialization(true);
        app.setWebApplicationType(WebApplicationType.NONE);
        app.run(args);
    }
}
