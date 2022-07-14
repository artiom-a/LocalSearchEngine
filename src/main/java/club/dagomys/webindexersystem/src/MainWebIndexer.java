package club.dagomys.webindexersystem.src;

import club.dagomys.siteparcer.src.MainSiteParser;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MainWebIndexer {
    public static void main(String[] args) {
        SpringApplication.run(MainSiteParser.class);
    }
}
