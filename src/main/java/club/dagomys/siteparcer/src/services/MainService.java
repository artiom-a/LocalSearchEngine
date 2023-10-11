package club.dagomys.siteparcer.src.services;


import club.dagomys.siteparcer.src.config.AppConfig;
import club.dagomys.siteparcer.src.dto.FieldSelector;
import club.dagomys.siteparcer.src.dto.Link;
import club.dagomys.siteparcer.src.dto.request.URLRequest;
import club.dagomys.siteparcer.src.dto.response.*;
import club.dagomys.siteparcer.src.entity.Field;
import club.dagomys.siteparcer.src.entity.Page;
import club.dagomys.siteparcer.src.entity.Site;
import club.dagomys.siteparcer.src.entity.SiteStatus;
import club.dagomys.siteparcer.src.exception.LemmaNotFoundException;
import club.dagomys.siteparcer.src.exception.PageIndexingException;
import club.dagomys.siteparcer.src.exception.SiteIndexingException;
import club.dagomys.siteparcer.src.lemmatisator.SiteParserRunner;
import club.dagomys.siteparcer.src.repositories.*;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.plugins.validation.constraints.Required;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class MainService {
    private final Logger mainLogger = LogManager.getLogger(MainService.class);
    private final AtomicBoolean isIndexing = new AtomicBoolean();
    private Response response = new Response();

    @Autowired
    private FieldRepository fieldRepository;

    @Autowired
    private LemmaRepository lemmaRepository;

    @Getter
    @Autowired
    private SearchIndexRepository searchIndexRepository;

    @Autowired
    private SiteRepository siteRepository;

    @Autowired
    private PageRepository pageRepository;

    @Autowired
    private ThreadPoolTaskExecutor asyncService;

    @Getter
    @Autowired
    private ForkJoinPool forkJoinPool;

    @Getter
    @Autowired
    private AppConfig appConfig;


    public Response startIndexingSites(boolean isAllSite, Site site) {
        try {
            List<SiteParserRunner> runList = new ArrayList<>();
            if (isAllSite) {
                siteRepository.findAll().forEach(s -> {
                    if (s.getStatus() != SiteStatus.INDEXING) {
                        isIndexing.set(true);
                        response.setResult(true);
                        runList.add(new SiteParserRunner(s, this));
                    } else {
                        isIndexing.set(false);
                        response.setResult(false);
                        response.setError(s.getUrl() + " is indexing");
                        mainLogger.error(s.getUrl() + " is indexing");
                    }
                });
                taskListener(runList);
            } else {
                response = getSiteIndexingResponse(site);
            }

            return response;
        } catch (Exception e) {
            site.setLastError(e.getMessage());
            siteRepository.saveAndFlush(site);
            mainLogger.error("Ошибка индексации " + e);
            isIndexing.set(false);
            response.setResult(false);
            response.setError(site.getUrl() + " status is " + site.getStatus());
            return response;
        }
    }

    private Response getSiteIndexingResponse(Site site) {
        List<SiteParserRunner> runList = new ArrayList<>();
        Response siteResponse = new Response();
        if (site.getStatus() != SiteStatus.INDEXING) {
            isIndexing.set(true);
            siteResponse.setResult(true);
            runList.add(new SiteParserRunner(site, this));
        } else {
            isIndexing.set(false);
            siteResponse.setResult(isIndexing.get());
            siteResponse.setError(site.getUrl() + " is indexing");
            mainLogger.error(site.getUrl() + " is indexing");
        }
        return siteResponse;
    }

    public Response stopIndexingSites() {
        if (!isIndexing.get()) {
            response.setResult(isIndexing.get());
            response.setError("Индексация не запущена");
        } else {
            isIndexing.set(false);
            siteRepository.findAll().forEach(site -> {
                if (site.getStatus() != SiteStatus.INDEXED) {
                    site.setStatusTime(LocalDateTime.now());
                    site.setStatus(SiteStatus.FAILED);
                    siteRepository.saveAndFlush(site);
                }
            });
            response.setResult(true);
            response.setError("Индексация останавливается");
        }
        return response;
    }

    public DashboardResponse getStatistic() {
        DashboardResponse response = new DashboardResponse();
        Statistic statistic = new Statistic();
        ArrayList<Detail> details = new ArrayList<>();
        Total total = new Total();

        AtomicInteger allLemmas = new AtomicInteger();
        AtomicInteger allPages = new AtomicInteger();

        List<Site> siteList = siteRepository.findAll();

        if (siteList.isEmpty()) {
            return new DashboardResponse();
        }

        siteList.forEach(site -> {
            try {
                int pages = pageRepository.findAllPageBySite(site).get().size();
                int lemmas = lemmaRepository.findAllLemmaBySite(site).get().size();
                allPages.updateAndGet(v -> v + pages);
                allLemmas.updateAndGet(v -> v + lemmas);
                details.add(new Detail(site.getUrl(), site.getName(), site.getStatus(), site.getStatusTime(), site.getLastError(), pages, lemmas));
            } catch (LemmaNotFoundException e) {
                mainLogger.error(e.getMessage());
            }
        });
        total.setLemmaCount(allLemmas.get());
        total.setPageCount(allPages.get());
        total.setSiteCount((int) siteRepository.count());
        total.setIndexing(isIndexing.get());
        statistic.setTotal(total);
        statistic.setSiteList(details);
        response.setResult(true);
        response.setStatistics(statistic);

        return response;
    }

    public Response reindexPage(URLRequest URL, @Required Errors error) {
        Response response = new Response();
        List<Site> siteList = siteRepository.findAll();
        Optional<Site> site = Optional.empty();
        Optional<Page> page = Optional.of(new Page());
        try {
            if (!error.hasErrors()) {
                Link rootLink = new Link(URL.getPath());
                for (Site s : siteList) {
                    if (s.getStatus() == SiteStatus.INDEXING) {
                        throw new PageIndexingException("Сайт " + s.getUrl() + " в процессе индексации");
                    }
                    if (rootLink.getValue().contains(s.getUrl())) {
                        site = Optional.of(s);
                        mainLogger.info(site);
                    }
                }
                if (site.isPresent()) {
                    String relativeURL = rootLink.getValue().replace(site.get().getUrl(), "");
                    page.get().setRelPath(relativeURL);
                    page.get().setSite(site.get());
                    Document pageFile = Jsoup
                            .connect(site.get().getUrl() + page.get().getRelPath())
                            .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                            .referrer("http://www.google.com")
                            .ignoreHttpErrors(false)
                            .get();
                    page.get().setContent(pageFile.outerHtml());
                    page.get().setStatusCode(pageFile.connection().response().statusCode());
                    pageRepository.saveAndFlush(page.get());
                    site.get().setStatusTime(LocalDateTime.now());
                    siteRepository.saveAndFlush(site.get());
                    response.setResult(true);
                } else throw new PageIndexingException("Такого сайта не существует");
            } else throw new PageIndexingException(Objects.requireNonNull(error.getFieldError()).getDefaultMessage());
        } catch (Exception | PageIndexingException e) {
            response.setResult(false);
            response.setError(e.getMessage());
            mainLogger.error(e.getMessage());
        }
        return response;
    }

    private void taskListener(List<SiteParserRunner> runList) {
        List<CompletableFuture<Void>> completableFutures = runList.parallelStream().map(task -> CompletableFuture.runAsync(task, asyncService.getThreadPoolExecutor())).toList();
        Thread taskListener = new Thread(() -> {
            while (isIndexing.get()) {
                try {
                    boolean isEveryRunnableDone = CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[completableFutures.size() - 1])).isDone();
                    if (isEveryRunnableDone) {
                        isIndexing.set(false);
                        mainLogger.info("All tasks is done!");
                    }
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    mainLogger.error(e.getMessage());
                }
            }
        });
        taskListener.setName("siteTaskListener");
        taskListener.start();
    }

    public Boolean isIndexing() {
        return isIndexing.get();
    }

    public FieldRepository getFieldService() {
        return fieldRepository;
    }

    public LemmaRepository getLemmaService() {
        return lemmaRepository;
    }

    public PageRepository getPageService() {
        return pageRepository;
    }

    public SiteRepository getSiteService() {
        return siteRepository;
    }


    /**
     * @param siteService  добавляет сайты из конфигурационного файла в БД. Если поле 'name' isEmpty,
     *                     то вместо названия сайта подставляется поле title главной страницы сайта.
     * @return добавляет 2 статические записи для полей на страницах сайтов со значениями по умолчанию
     */
    @Bean
    public CommandLineRunner saveSiteToDb(SiteRepository siteService) throws SiteIndexingException {
        return (String[] args) -> {
            Field title = new Field("title", FieldSelector.TITLE, 1f);
            Field body = new Field("body", FieldSelector.BODY, 0.8f);
            if (this.fieldRepository.findAll().size() < 2) {
                this.fieldRepository.save(title);
                this.fieldRepository.save(body);
            } else {
                mainLogger.info("Данные уже добавлены ранее");
                this.fieldRepository.findAll().forEach(mainLogger::info);
            }
            appConfig.getSiteList().forEach(site -> {
                if (site.getUrl().endsWith("/")) {
                    site.setUrl(site.getUrl().strip().replaceFirst(".$", ""));
                }
                Optional<Site> findSite = siteRepository.findByUrl(site.getUrl());
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
                        siteService.saveAndFlush(site);
                    } else {
                        siteService.saveAndFlush(site);
                    }
                } else {
                    siteService.saveAndFlush(findSite.get());
                }
            });
        };
    }
}
