package club.dagomys.siteparcer.src.services;


import club.dagomys.siteparcer.src.config.AppConfig;
import club.dagomys.siteparcer.src.dto.FieldSelector;
import club.dagomys.siteparcer.src.dto.response.Response;
import club.dagomys.siteparcer.src.entity.Field;
import club.dagomys.siteparcer.src.entity.Site;
import club.dagomys.siteparcer.src.entity.SiteStatus;
import club.dagomys.siteparcer.src.exception.SiteIndexingException;
import club.dagomys.siteparcer.src.repositories.*;
import club.dagomys.siteparcer.src.utils.siteparser.SiteParserRunner;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@Getter
@Slf4j
public class IndexingService {

    private final AtomicBoolean isIndexing = new AtomicBoolean();

    private Response response = new Response();

    @Autowired
    private FieldRepository fieldRepository;

    @Autowired
    private LemmaRepository lemmaRepository;

    @Autowired
    private SearchIndexRepository searchIndexRepository;

    @Autowired
    private SiteRepository siteRepository;

    @Autowired
    private PageRepository pageRepository;

    @Autowired
    private ThreadPoolTaskExecutor asyncService;

    @Autowired
    private ForkJoinPool forkJoinPool;

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
                        log.error(s.getUrl() + " is indexing");
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
            log.error("Ошибка индексации " + e);
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
            log.error(site.getUrl() + " is indexing");
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

    private void taskListener(List<SiteParserRunner> runList) {
        List<CompletableFuture<Void>> completableFutures = runList.parallelStream().map(task -> CompletableFuture.runAsync(task, asyncService.getThreadPoolExecutor())).toList();
        Thread taskListener = new Thread(() -> {
            while (isIndexing.get()) {
                try {
                    boolean isEveryRunnableDone = CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[completableFutures.size() - 1])).isDone();
                    if (isEveryRunnableDone) {
                        isIndexing.set(false);
                        log.info("All tasks is done!");
                    }
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    log.error(e.getMessage());
                }
            }
        });
        taskListener.setName("siteTaskListener");
        taskListener.start();
    }


    /**
     * @param siteService добавляет сайты из конфигурационного файла в БД. Если поле 'name' isEmpty,
     *                    то вместо названия сайта подставляется поле title главной страницы сайта.
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
                log.info("Данные уже добавлены ранее");
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
                            log.error(site.getUrl() + " " + e.getMessage());
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
