package club.dagomys.siteparcer.src.services;

import club.dagomys.siteparcer.src.config.AppConfig;
import club.dagomys.siteparcer.src.config.AsyncConfig;
import club.dagomys.siteparcer.src.entity.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.*;

@Service
public class MainService {
    private final Logger mainLogger = LogManager.getLogger(MainService.class);


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
    private AppConfig appConfig;

//    @Async("taskExecutor")
    public void startIndexingSites(boolean isAllSite, Site site) {
        if (isAllSite) {

            siteService.getAllSites().join().parallelStream().forEach(s ->{
                SiteParserRunner parser = new SiteParserRunner(s, this);
                    parser.run();
            });
            mainLogger.info("SITE PARSING IS FINISHED!");
        } else {
            SiteParserRunner parser = new SiteParserRunner(site, this);
            if (parser.isStarted().get()){
                mainLogger.info("parser is running...");
            } else {
                parser.run();
            }
        }
    }

    public void stopIndexingSites() {
        try {
//            asyncConfig.getAsyncExecutor().getThreadPoolExecutor().shutdownNow();

        } catch (Exception interruptedException){
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
