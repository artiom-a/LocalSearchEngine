package club.dagomys.siteparcer.src.services;

import club.dagomys.siteparcer.src.entity.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@Service
public class MainService {
    private final Logger mainLogger = LogManager.getLogger(MainService.class);
    private final List<SiteParserRunner> siteParserRunnerList = new ArrayList<>();


    @Autowired
    private FieldService fieldService;

    @Autowired
    private LemmaService lemmaService;

    @Autowired
    private SearchIndexService searchIndexService;

    @Autowired
    private SearchService searchService;

    @Autowired
    private SiteService siteService;

    @Autowired
    private PageService pageService;


    public void startIndexingSites(boolean isAllSite, Site site) {
        if (isAllSite) {

            siteService.getAllSites().parallelStream().forEach(s -> {
                if (s.getStatus() != SiteStatus.INDEXING) {

                    new SiteParserRunner(s, this).run();
//                    Thread t = new Thread(new SiteParserRunner(s, this));
//                    t.setName(s.getId()+"- Thread");
//                    t.start();
                } else {
                    mainLogger.error(s.getUrl() + " is indexing");
                }
            });
        } else {
            SiteParserRunner parser = new SiteParserRunner(site, this);
            if (site.getStatus() == SiteStatus.INDEXING) {
                mainLogger.info("parser is running...");
            } else {
                parser.run();
            }
        }
        mainLogger.info("SITE PARSING IS FINISHED!");
    }

    public void stopIndexingSites() {
        siteParserRunnerList.forEach(SiteParserRunner::doStop);
        try {
//            asyncConfig.getAsyncExecutor().getThreadPoolExecutor().shutdownNow();

        } catch (Exception interruptedException) {
            mainLogger.warn("App is stopped! " + interruptedException.getMessage());
        }
    }

    public FieldService getFieldService() {
        return fieldService;
    }

    public LemmaService getLemmaService() {
        return lemmaService;
    }

    public PageService getPageService() {
        return pageService;
    }

    public SiteService getSiteService() {
        return siteService;
    }

    public SearchIndexService getSearchIndexService() {
        return searchIndexService;
    }

}
