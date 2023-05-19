package club.dagomys.siteparcer.src.services;


import club.dagomys.siteparcer.src.entity.*;
import club.dagomys.siteparcer.src.entity.request.URLRequest;
import club.dagomys.siteparcer.src.entity.response.DashboardResponse;
import club.dagomys.siteparcer.src.entity.response.Detail;
import club.dagomys.siteparcer.src.entity.response.Statistic;
import club.dagomys.siteparcer.src.entity.response.Total;
import club.dagomys.siteparcer.src.exception.PageIndexingException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class MainService {
    private final Logger mainLogger = LogManager.getLogger(MainService.class);
    AtomicBoolean isIndexing = new AtomicBoolean();


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

    @Autowired
    private ForkJoinPool forkJoinPool;


    public boolean startIndexingSites(boolean isAllSite, Site site) {
        try {
            if (isAllSite) {
                siteService.getAllSites().forEach(s -> {
                    if (s.getStatus() != SiteStatus.INDEXING) {
                        isIndexing.set(true);
                        asyncService.submit(new SiteParserRunner(s, this));
                    } else {
                        isIndexing.set(false);
                        mainLogger.error(s.getUrl() + " is indexing");
                    }
                });
            } else {
                if (site.getStatus() != SiteStatus.INDEXING) {
                    isIndexing.set(true);
                    asyncService.submit(new SiteParserRunner(site, this));
                } else {
                    isIndexing.set(false);
                    mainLogger.info(site.getUrl() + " status is " + site.getStatus());
                }
            }
            return isIndexing.get();
        } catch (Exception e) {
            mainLogger.error("Ошибка индексации " + e);
            isIndexing.set(false);
            return isIndexing.get();
        }
    }

    public boolean stopIndexingSites() {
        try {
            if (!asyncService.getThreadPoolExecutor().awaitTermination(1, TimeUnit.SECONDS)) {
                asyncService.getThreadPoolExecutor().shutdownNow();
                forkJoinPool.shutdownNow();
                if (!asyncService.getThreadPoolExecutor().awaitTermination(1, TimeUnit.SECONDS))
                    mainLogger.error("Pool did not terminate");
            }
            siteService.getAllSites().forEach(site -> {
                site.setStatusTime(LocalDateTime.now());
                site.setStatus(SiteStatus.FAILED);
                siteService.saveOrUpdate(site);
            });
        } catch (InterruptedException ie) {
            mainLogger.error(ie.getMessage());
        }
        isIndexing.set(false);
        return isIndexing.get();
    }

    public DashboardResponse getStatistic() {
        DashboardResponse response = new DashboardResponse();
        Statistic statistic = new Statistic();
        ArrayList<Detail> details = new ArrayList<>();
        Total total = new Total();

        AtomicInteger allLemmas = new AtomicInteger();
        AtomicInteger allPages = new AtomicInteger();

        List<Site> siteList = siteService.getAllSites();

        if (siteList.size() == 0) {
            return new DashboardResponse();
        }

        siteList.forEach(site -> {
            int pages = pageService.getPagesBySite(site).size();
            int lemmas = lemmaService.getLemmaList(site).get().size();
            allPages.updateAndGet(v -> v + pages);
            allLemmas.updateAndGet(v -> v + lemmas);
            details.add(new Detail(site.getUrl(), site.getName(), site.getStatus(), site.getStatusTime(), site.getLastError(), pages, lemmas));
        });
        total.setLemmaCount(allLemmas.get());
        total.setPageCount(allPages.get());
        total.setSiteCount(siteService.getSiteCount());
        total.setIndexing(isIndexing.get());
        statistic.setTotal(total);
        statistic.setSiteList(details);
        response.setResult(true);
        response.setStatistics(statistic);

        return response;
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

    public ForkJoinPool getForkJoinPool() {
        return forkJoinPool;
    }

}
