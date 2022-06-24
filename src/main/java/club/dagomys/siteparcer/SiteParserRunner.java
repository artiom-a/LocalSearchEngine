package club.dagomys.siteparcer;

import club.dagomys.siteparcer.src.entity.Link;
import club.dagomys.siteparcer.src.SiteParser;
import club.dagomys.siteparcer.src.entity.MainLog4jLogger;
import club.dagomys.siteparcer.src.entity.Page;
import club.dagomys.siteparcer.src.services.PageService;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;

@Component
public class SiteParserRunner implements CommandLineRunner {
    @Autowired
    private PageService pageService;

    Logger mainLogger = MainLog4jLogger.getIstance();
    @Override
    public void run(String... args) {
        System.out.println("Command line is running");
        try {

            final String URL = "https://www.svetlovka.ru/";

            Link rootLink = new Link(URL);
            ForkJoinPool siteMapPool = new ForkJoinPool(Runtime.getRuntime().availableProcessors());
            ForkJoinTask<Link> forkJoinTask = new SiteParser(rootLink);
            siteMapPool.invoke(forkJoinTask);
            insertToDatabase(rootLink);
            System.out.println("Command line runner is finished");
        } catch (IOException e) {
            System.out.println(e.getMessage());

        }
    }

    public String createSitemap(Link node) {
        String tabs = String.join("", Collections.nCopies(node.getLayer(), "\t"));
        StringBuilder result = new StringBuilder(tabs + node.getRelUrl());
        node.getChildren().forEach(child -> {
            result.append("\n").append(createSitemap(child));
        });
        return result.toString();
    }

    public void insertToDatabase(Link link) {
        Page root = new Page(link.getRelUrl());
        root.setStatusCode(link.getStatusCode());
        root.setContent(link.getHtml());
        pageService.savePage(root);
        link.getChildren().forEach(this::insertToDatabase);
    }

}
