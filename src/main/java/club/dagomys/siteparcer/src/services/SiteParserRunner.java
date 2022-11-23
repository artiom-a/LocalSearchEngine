package club.dagomys.siteparcer.src.services;

import club.dagomys.lemmatisator.scr.LemmaCounter;
import club.dagomys.siteparcer.src.entity.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;

@Component
public class SiteParserRunner implements Runnable {
    private Map<String, Lemma> lemmaMap = new ConcurrentHashMap<>();
    private final Logger mainLogger = LogManager.getLogger(SiteParserRunner.class);
    private final Site site;
    private static boolean isStarted = false;


    @Autowired
    private final MainService mainService;

    @Autowired
    public SiteParserRunner(@Value("${site.name}") Site site, MainService mainService) {
        this.mainService = mainService;
        this.site = site;
    }

    private synchronized void setStarted(boolean started) {
        isStarted = started;
    }

    public synchronized boolean isStarted() {
        return isStarted;
    }

    @Override
    public void run() {
        setStarted(true);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yy hh:mm:ss");
        Calendar startTime = Calendar.getInstance();
        mainLogger.warn("start time " + dateFormat.format(startTime.getTime()));
        try {
            Link rootLink = new Link(site.getUrl());
            ForkJoinPool siteMapPool = new ForkJoinPool(Runtime.getRuntime().availableProcessors());
            ForkJoinTask<Link> forkJoinTask = new SiteParser(rootLink);
            siteMapPool.invoke(forkJoinTask);
            insertToDatabase(rootLink, site);
            String title = Jsoup.parse(rootLink.getHtml()).title();
            site.setName(title);
            mainService.getSiteService().saveSite(site);
            Calendar finishDate = Calendar.getInstance();
            mainLogger.warn("finish time " + dateFormat.format(finishDate.getTime()));
            Duration duration = Duration.between(startTime.toInstant(), finishDate.toInstant());
            mainLogger.info("Duration " + duration.toString());
            setStarted(false);
        } catch (IOException e) {
            System.out.println(e.getMessage());

        }
    }

    private void insertToDatabase(Link link, Site site) {
        Page root = new Page(link.getRelUrl());
        root.setStatusCode(link.getStatusCode());
        root.setContent(link.getHtml());
        root.setSite(site);
        mainService.getPageService().savePage(root);
        link.getChildren().forEach(child ->
                {
                    insertToDatabase(child, site);
                }
        );
    }
}
