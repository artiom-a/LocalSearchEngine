package club.dagomys.siteparcer.src;

import club.dagomys.siteparcer.src.entity.Link;
import club.dagomys.siteparcer.src.SiteParser;
import club.dagomys.siteparcer.src.entity.MainLog4jLogger;
import club.dagomys.siteparcer.src.entity.Page;
import club.dagomys.siteparcer.src.services.PageService;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.analysis.morphology.MorphologyFilterFactory;
import org.apache.lucene.morphology.MorphologyImpl;
import org.apache.lucene.morphology.analyzer.MorphologyFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
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
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yy hh:mm:ss");
        Calendar startTime = Calendar.getInstance();
        mainLogger.warn("start time " + dateFormat.format(startTime.getTime()));
        try {

            final String URL = "https://skillbox.ru/ ";

            Link rootLink = new Link(URL);
            ForkJoinPool siteMapPool = new ForkJoinPool(Runtime.getRuntime().availableProcessors());
            ForkJoinTask<Link> forkJoinTask = new SiteParser(rootLink);
            siteMapPool.invoke(forkJoinTask);
            insertToDatabase(rootLink);
            Calendar finishDate = Calendar.getInstance();
            mainLogger.warn("finish time " + dateFormat.format(finishDate.getTime()));
            Duration duration = Duration.between(startTime.toInstant(), finishDate.toInstant());
            mainLogger.info("Duration " + duration.toString());
        } catch (IOException e) {
            System.out.println(e.getMessage());

        }
    }

    private String createSitemap(Link node) {
        String tabs = String.join("", Collections.nCopies(node.getLayer(), "\t"));
        StringBuilder result = new StringBuilder(tabs + node.getRelUrl());
        node.getChildren().forEach(child -> {
            result.append("\n").append(createSitemap(child));
        });
        return result.toString();
    }

    private void insertToDatabase(Link link) {
        Page root = new Page(link.getRelUrl());
        root.setStatusCode(link.getStatusCode());
        root.setContent(link.getHtml());
        pageService.savePage(root);
        link.getChildren().forEach(this::insertToDatabase);
    }

}
