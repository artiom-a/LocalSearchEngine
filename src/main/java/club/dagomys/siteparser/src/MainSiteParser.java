package club.dagomys.siteparser.src;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

import java.io.IOException;

@SpringBootApplication
@OpenAPIDefinition
public class MainSiteParser extends SpringBootServletInitializer {
    public static void main(String[] args) throws IOException {
        SpringApplication app = new SpringApplication(MainSiteParser.class);
        app.setBannerMode(Banner.Mode.OFF);
        app.setLazyInitialization(true);
        app.run(args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(MainSiteParser.class);
    }
}
