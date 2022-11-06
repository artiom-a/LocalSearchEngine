package club.dagomys.siteparcer.src.services;

import club.dagomys.siteparcer.src.entity.Link;
import club.dagomys.siteparcer.src.entity.Site;
import club.dagomys.siteparcer.src.repos.SiteRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Calendar;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;

@Component
public class SiteParserRunner {
    private final Logger mainLogger = LogManager.getLogger(SiteParserRunner.class);
    @Autowired
    private PageService pageService;

    @Autowired
    private SiteService siteService;
    private final String URL;
    private static boolean isStarted = false;

    @Autowired
    public SiteParserRunner(@Value("${site.name}") String URL, PageService service, SiteService siteService) {
        this.pageService = service;
        this.siteService = siteService;
        this.URL = URL;
    }

    public void run(String... args) {
        setStarted(true);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yy hh:mm:ss");
        Calendar startTime = Calendar.getInstance();
        mainLogger.warn("start time " + dateFormat.format(startTime.getTime()));
        try {
            Link rootLink = new Link(URL);
            siteService.saveSite(new Site(URL, rootLink.getValue()));
            ForkJoinPool siteMapPool = new ForkJoinPool(Runtime.getRuntime().availableProcessors());
            ForkJoinTask<Link> forkJoinTask = new SiteParser(rootLink);
            siteMapPool.invoke(forkJoinTask);
            pageService.insertToDatabase(rootLink);
            Calendar finishDate = Calendar.getInstance();
            mainLogger.warn("finish time " + dateFormat.format(finishDate.getTime()));
            Duration duration = Duration.between(startTime.toInstant(), finishDate.toInstant());
            mainLogger.info("Duration " + duration.toString());
            setStarted(false);
        } catch (IOException e) {
            System.out.println(e.getMessage());

        }
    }

    private void setStarted(boolean started) {
        isStarted = started;
    }

    public boolean isStarted() {
        return isStarted;
    }

}
