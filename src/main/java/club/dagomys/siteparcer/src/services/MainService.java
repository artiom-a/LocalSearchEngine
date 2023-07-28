package club.dagomys.siteparcer.src.services;


import club.dagomys.siteparcer.src.config.AppConfig;
import club.dagomys.siteparcer.src.entity.*;
import club.dagomys.siteparcer.src.entity.response.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;


import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class MainService {
    private final Logger mainLogger = LogManager.getLogger(MainService.class);
    private final AtomicBoolean isIndexing = new AtomicBoolean();
    private final List<SiteParserRunner> runList = new ArrayList<>();
    private List<CompletableFuture<Void>> completableFutures = new ArrayList<>();
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

    @Autowired
    private AppConfig appConfig;


    public Response startIndexingSites(boolean isAllSite, Site site) {
        Response response = new Response();
        try {
            if (isAllSite) {
                siteService.getAllSites().forEach(s -> {
                    if (s.getStatus() != SiteStatus.INDEXING) {
                        isIndexing.set(true);
                        response.setResult(true);
                        runList.add(new SiteParserRunner(s, this));
                    } else {
                        isIndexing.set(false);
                        response.setResult(false);
                        response.setError(s.getUrl()+"is indexing");
                        mainLogger.error(s.getUrl() + " is indexing");
                    }
                });
                taskListener(runList);
            } else {
                if (site.getStatus() != SiteStatus.INDEXING) {
                    isIndexing.set(true);
                    response.setResult(true);
                    asyncService.submit(new SiteParserRunner(site, this));
                } else {
                    response.setResult(false);
                    response.setError(site.getUrl() + " status is " + site.getStatus());
                    mainLogger.info(site.getUrl() + " status is " + site.getStatus());
                }
            }
            return response;
        } catch (Exception e) {
            site.setLastError(e.getMessage());
            siteService.saveOrUpdate(site);
            mainLogger.error("Ошибка индексации " + e);
            isIndexing.set(false);
            response.setResult(false);
            response.setError(site.getUrl() + " status is " + site.getStatus());
            return response;
        }
    }

    private Response getSiteIndexingResponse(Site site) {
        Response siteResponse = new Response();
        if (site.getStatus() != SiteStatus.INDEXING) {
            isIndexing.set(true);
            siteResponse.setResult(true);
            asyncService.submit(new SiteParserRunner(site, this));
        } else {
            isIndexing.set(false);
            siteResponse.setResult(isIndexing.get());
            siteResponse.setError(site.getUrl() + " is indexing");
            mainLogger.error(site.getUrl() + " is indexing");
        }
        return siteResponse;
    }

    public Response stopIndexingSites() {
        Response response = new Response();
//        completableFutures = runList.stream().map(task -> CompletableFuture.runAsync(task, asyncService.getThreadPoolExecutor())).toList();
        try {
            runList.parallelStream().forEach(SiteParserRunner::doStop);
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        siteService.getAllSites().forEach(site -> {
            if (site.getStatus() != SiteStatus.INDEXED) {
                site.setStatusTime(LocalDateTime.now());
                site.setStatus(SiteStatus.FAILED);
                siteService.saveOrUpdate(site);
            }
        });

        isIndexing.set(false);
        response.setResult(false);
        response.setError("parsing is stopped");
        return response;
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

    private void taskListener(List<SiteParserRunner> runList) {
        completableFutures = runList.stream().map(task -> CompletableFuture.runAsync(task, asyncService.getThreadPoolExecutor())).toList();
        Thread taskListener = new Thread(() -> {
            while (isIndexing.get()) {
                boolean isEveryRunnableDone = CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[completableFutures.size() - 1])).isDone();
                if (isEveryRunnableDone) {
                    mainLogger.info("All tasks is done!");
                    isIndexing.set(false);
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        taskListener.setName("taskListener");
        taskListener.start();
        if (!isIndexing.get()) {
            taskListener.interrupt();
        }
    }

    public Boolean isIndexing() {
        return isIndexing.get();
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

    public AppConfig getAppConfig() {
        return appConfig;
    }

    @Bean
    public CommandLineRunner saveSiteToDb(SiteService siteService) throws Exception {
        return (String[] args) -> {
            appConfig.getSiteList().forEach(site -> {
                if (site.getUrl().endsWith("/")) {
                    site.setUrl(site.getUrl().strip().replaceFirst(".$", ""));
                }
                Optional<Site> findSite = siteService.getSite(site.getUrl());
                if (findSite.isEmpty()) {
                    if (site.getName().isEmpty()) {
                        try {
                            Document siteFile = Jsoup
                                    .connect(site.getUrl())
                                    .userAgent(appConfig.getUserAgent())
                                    .referrer("http://www.google.com")
                                    .ignoreHttpErrors(true)
                                    .get();
                            site.setName(siteFile.title());
                        } catch (IOException e) {
                            site.setLastError("Site is not found");
                            mainLogger.error(site.getUrl() + " " + e.getMessage());
                        }
                        siteService.saveOrUpdate(site);
                    } else {
                        siteService.saveSite(site);
                    }
                } else {
                    siteService.saveOrUpdate(findSite.get());
                }
            });
        };
    }
}
