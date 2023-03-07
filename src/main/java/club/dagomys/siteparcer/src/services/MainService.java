package club.dagomys.siteparcer.src.services;

import club.dagomys.siteparcer.src.entity.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

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

    @Autowired
    private ThreadPoolTaskExecutor asyncService;


    public void startIndexingSites(boolean isAllSite, Site site) {
        if (isAllSite) {
            siteService.getAllSites().forEach(s -> {
                if (s.getStatus() != SiteStatus.INDEXING) {
                    asyncService.getThreadPoolExecutor().execute(new SiteParserRunner(s, this));
                } else {
                    mainLogger.error(s.getUrl() + " is indexing");
                }
            });
        } else {
            SiteParserRunner parser = new SiteParserRunner(site, this);
            if (site.getStatus() == SiteStatus.INDEXING) {
                mainLogger.info(site.getUrl() + " status is " + site.getStatus());
            } else {
                asyncService.getThreadPoolExecutor().execute(parser);
            }
        }
        mainLogger.info("SITE PARSING IS FINISHED!");
    }

    public void stopIndexingSites() {
        siteParserRunnerList.forEach(SiteParserRunner::doStop);
        try {
            asyncService.getThreadPoolExecutor().shutdownNow();

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
