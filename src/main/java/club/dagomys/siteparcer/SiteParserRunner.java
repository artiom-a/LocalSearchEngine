package club.dagomys.siteparcer;

import club.dagomys.siteparcer.src.entity.Link;
import club.dagomys.siteparcer.src.SiteParser;
import club.dagomys.siteparcer.src.entity.Page;
import club.dagomys.siteparcer.src.services.PageService;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;

@Component
public class SiteParserRunner implements CommandLineRunner {
    @Autowired
    private PageService pageService;

//    @Value("${site.name}")
//    private String URL;


    @Override
    public void run(String... args) {
        System.out.println("Command line is running");
        try {
            final File siteMap = new File("src/main/java/output/siteMap.txt");
            final FileWriter fw = new FileWriter(siteMap);
            final String URL = "http://www.playback.ru/";

            Link mainLink = new Link(URL);
            ForkJoinPool siteMapPool = new ForkJoinPool(Runtime.getRuntime().availableProcessors());
            ForkJoinTask<Link> forkJoinTask = new SiteParser(mainLink);
            siteMapPool.invoke(forkJoinTask);

            insertToDatabase(mainLink);
//            fw.write(createSitemap(mainLink));

//            fw.flush();
//            fw.close();
            System.out.println("Command line runner is finished");
        } catch (IOException e) {
            System.out.println(e.getMessage());

        }
    }

    public String createSitemap(Link node) {
        String tabs = String.join("", Collections.nCopies(node.getLayer(), "\t"));
        StringBuilder result = new StringBuilder(tabs + node.getValue());
        pageService.savePage(new Page(node.getRelUrl()));
        node.getChildren().forEach(child -> {
            pageService.savePage(new Page(child.getRelUrl()));
            result.append("\n").append(createSitemap(child));
        });
        return result.toString();
    }

    public void insertToDatabase(Link link){
        pageService.savePage(new Page(link.getRelUrl()));
        link.getChildren().forEach(child -> {
            Page childPage = new Page(child.getRelUrl());
            childPage.setStatusCode(child.getStatusCode());
            childPage.setContent(link.getHtml());
            pageService.savePage(childPage);
        });
    }

}
