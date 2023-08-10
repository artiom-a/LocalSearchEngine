package club.dagomys.siteparcer.src.services;

import club.dagomys.siteparcer.src.exception.SiteIndexingException;
import club.dagomys.siteparcer.src.lemmatisator.LemmaCounter;
import club.dagomys.siteparcer.src.entity.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;

@Component
public class SiteParserRunner implements Runnable {
    private final Logger mainLogger = LogManager.getLogger(SiteParserRunner.class);
    private final int CORE_COUNT = Runtime.getRuntime().availableProcessors();
    private final Site site;
    private volatile boolean isRunning;

    @Autowired
    private final MainService mainService;

    @Autowired
    private ExecutorService asyncService;

    public SiteParserRunner(Site site, MainService mainService) {
        this.mainService = mainService;
        this.site = site;
    }


    @Override
    public void run() {
        this.isRunning = true;
        site.setStatus(SiteStatus.INDEXING);
        site.setStatusTime(LocalDateTime.now());
        mainService.getSiteService().saveOrUpdate(this.site);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yy HH:mm:ss");
        Calendar startTime = Calendar.getInstance();
        mainLogger.warn("start time " + dateFormat.format(startTime.getTime()));
        while (isRunning) {
            try {
                Link siteLinks = getSiteLinks();
                saveToDatabase(siteLinks);
                countLemmaFrequency();
                createSearchSiteIndexes();
                this.isRunning = false;
                Calendar finishDate = Calendar.getInstance();
                mainLogger.warn("finish time " + dateFormat.format(finishDate.getTime()));
                Duration duration = Duration.between(startTime.toInstant(), finishDate.toInstant());
                mainLogger.info("Duration " + duration.toString());
                site.setStatusTime(LocalDateTime.now());
                site.setStatus(SiteStatus.INDEXED);
                mainService.getSiteService().saveOrUpdate(this.site);
            } catch (SiteIndexingException e) {
                this.isRunning = false;
                site.setStatus(SiteStatus.FAILED);
                site.setLastError(e.getMessage());
                mainService.getSiteService().saveOrUpdate(site);
                mainLogger.error(e.getMessage() + " " + "Поток был прерван пользователем");
            }

        }


    }


    private void countLemmaFrequency() throws SiteIndexingException {
        Map<String, Integer> indexedPagesLemmas = new TreeMap<>();
        for (Page page : mainService.getPageService().getPagesBySite(this.site)) {
            Map<String, Lemma> indexedPageMap = countLemmasOnPage(page);
            if (mainService.isIndexing()) {
                mainLogger.info("Start parsing \t" + page.getRelPath());
                indexedPageMap.forEach((key, value) -> {
                    if (indexedPagesLemmas.containsKey(key)) {
                        indexedPagesLemmas.put(key, indexedPagesLemmas.get(key) + 1);
                    } else {
                        indexedPagesLemmas.put(key, 1);
                    }
                });
            } else throw new SiteIndexingException("Нахождение частоты лемм на сайте остановлено");
            lemmaCounting(indexedPageMap);
            mainLogger.warn("End parsing \t" + page.getRelPath());
        }
        saveLemmaToDatabase(indexedPagesLemmas);
    }

    private void saveLemmaToDatabase(Map<String, Integer> lemmaMap) {
        Set<Lemma> lemmaList = new TreeSet<>();
        lemmaMap.forEach((key, value) -> {
            try {
                if (mainService.isIndexing()) {
                    Lemma lemma = new Lemma(key, value);
                    lemma.setSite(site);
                    lemmaList.add(lemma);
                } else throw new SiteIndexingException("Сохранение лемм в БД остановлено для сайта " + site.getUrl());
            } catch (SiteIndexingException e) {
                mainLogger.error(e.getMessage());
            }
        });
        mainService.getLemmaService().deleteAllBySite(site);
        mainService.getLemmaService().saveAllLemmas(lemmaList);
    }

    private Map<String, Lemma> countLemmasOnPage(@NotNull Page indexingPage) throws SiteIndexingException {
        Map<String, Lemma> lemmas = new TreeMap<>();
        if (indexingPage.getContent() != null) {
            Document doc = Jsoup.parse(indexingPage.getContent());
            try {
                if (mainService.isIndexing()) {
                    for (Field field : mainService.getFieldService().getAllFields()) {
                        LemmaCounter lemmaCounter = new LemmaCounter(doc.getElementsByTag(field.getName()).text());
                        lemmaCounter.countLemmas().forEach((key, value) -> lemmas.merge(key, new Lemma(key, value), Lemma::sum));
                    }
                } else throw new SiteIndexingException("Остановка парсинга страницы");
            } catch (IncorrectResultSizeDataAccessException | IOException e) {
                System.out.println(e.getMessage());
            }
        } else {
            mainLogger.warn(indexingPage + " is not available. Code " + indexingPage.getStatusCode());
        }
        mainLogger.info("Lemmas count: {} {} ", lemmas.size(), "words");
        return lemmas;
    }


    private void lemmaCounting(Map<String, Lemma> inputMap) {
        Map<String, Lemma> lemmaMap = new TreeMap<>();
        inputMap.forEach((key, value) -> {
            if (lemmaMap.containsKey(key)) {
                lemmaMap.put(key, new Lemma(key, lemmaMap.get(key).getFrequency() + value.getFrequency()));
            } else {
                lemmaMap.put(key, value);
            }
        });
    }

    private Map<String, Float> startIndexingLemmasOnPage(@NotNull Page indexingPage) {

        Map<String, Float> lemmas = new TreeMap<>();

        Field title = mainService.getFieldService().getFieldByName(FieldSelector.TITLE);
        Field body = mainService.getFieldService().getFieldByName(FieldSelector.BODY);
        if (indexingPage.getContent() != null) {
            Document doc = Jsoup.parse(indexingPage.getContent());
            try {
                LemmaCounter titleCounter = new LemmaCounter(doc.getElementsByTag(title.getName()).text());
                LemmaCounter bodyCounter = new LemmaCounter(doc.getElementsByTag(body.getName()).text());
                Map<String, Integer> titleLemmas = titleCounter.countLemmas();
                Map<String, Integer> bodyLemmas = bodyCounter.countLemmas();
                bodyLemmas.forEach((key, value) -> {
                    float rank;
                    if (titleLemmas.containsKey(key)) {
                        rank = title.getWeight() * titleLemmas.get(key) + value * body.getWeight();
                    } else {
                        rank = bodyLemmas.get(key) * body.getWeight();
                    }
                    lemmas.put(key, rank);
                });
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        } else {
            mainLogger.warn(indexingPage + " is not available. Code " + indexingPage.getStatusCode());
        }

        mainLogger.info("lemmas size\t {}", lemmas.size());
        return lemmas;

    }

    private void createSearchSiteIndexes() throws SiteIndexingException {
        List<Lemma> lemmas = mainService.getLemmaService().getLemmaList(site).orElseThrow();
        for (Page page : mainService.getPageService().getPagesBySite(this.site)) {
            mainService.getSearchIndexService().deleteByPage(page);

            long startTime = System.currentTimeMillis();
            mainLogger.info("Start parsing \t" + this.site.getUrl() + "" + page.getRelPath());
            if (mainService.isIndexing()) {
                Map<String, Float> indexedPageMap = startIndexingLemmasOnPage(page);
                indexedPageMap.forEach((key, value) -> {
                    Lemma findLemma = lemmas.parallelStream().filter(l -> l.getLemma().equalsIgnoreCase(key)).findFirst().orElseThrow();
                    mainService.getSearchIndexService().saveIndex(new SearchIndex(page, findLemma, value));
                });
                long endTime = System.currentTimeMillis();
                mainLogger.warn(page.getRelPath() + " indexing is complete");
                mainLogger.warn("End parsing {} ms \t" + page.getRelPath(), endTime - startTime);
            } else
                throw new SiteIndexingException("Индексация остановлена на странице " + page.getRelPath() + " сайт " + page.getSite().getUrl());

        }
    }


    private Link getSiteLinks() throws SiteIndexingException {
        if (mainService.isIndexing()) {
            Link rootLink = new Link(this.site.getUrl());
            mainService.getSiteService().saveSite(this.site);
            RecursiveTask<Link> forkJoinTask = new SiteParser(rootLink, mainService, this.site);
            return mainService.getForkJoinPool().invoke(forkJoinTask);
        } else throw new SiteIndexingException("Парсинг ссылок остановлен " + this.site.getUrl());
    }


    private void saveToDatabase(Link link) throws SiteIndexingException {
        Page root = new Page(link);

        mainService.getPageService().saveOrUpdate(root);
        for (Link l : link.getChildren()) {
            if (mainService.isIndexing()) {
                saveToDatabase(l);
            } else {
                throw new SiteIndexingException("Не удалось сохранить страницу " + link.getValue());
            }
        }


    }

    public void doStop() {
        this.isRunning = false;
    }

}
