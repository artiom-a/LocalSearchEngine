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
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

@Component
public class SiteParserRunner implements Runnable {
    private final Logger mainLogger = LogManager.getLogger(SiteParserRunner.class);
    private final int CORE_COUNT = Runtime.getRuntime().availableProcessors();
    ForkJoinPool siteMapPool = new ForkJoinPool(CORE_COUNT);
    private Site site;
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
        site.setStatus(SiteStatus.INDEXING);
        site.setStatusTime(LocalDateTime.now());
        mainService.getSiteService().updateSite(site);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yy hh:mm:ss");
        Calendar startTime = Calendar.getInstance();
        mainLogger.warn("start time " + dateFormat.format(startTime.getTime()));
        try {
            startSiteParse(site);
            countLemmaFrequency(site);
            createSearchSiteIndexes(site);
        } catch (Exception ex) {
            mainLogger.error(ex.getMessage());
        }

        Calendar finishDate = Calendar.getInstance();
        mainLogger.warn("finish time " + dateFormat.format(finishDate.getTime()));
        Duration duration = Duration.between(startTime.toInstant(), finishDate.toInstant());
        mainLogger.info("Duration " + duration.toString());
        site.setStatusTime(LocalDateTime.now());
        site.setStatus(SiteStatus.INDEXED);
        mainService.getSiteService().updateSite(site);
        setStarted(false);
    }


    private Map<String, Integer> countLemmaFrequency(Site site) {
        Map<String, Integer> indexedPagesLemmas = new TreeMap<>();
        for (Page page : mainService.getPageService().getPagesBySite(site)) {
            mainLogger.info("Start parsing \t" + page.getRelPath());
            Map<String, Lemma> indexedPageMap = countLemmasOnPage(page);
            indexedPageMap.forEach((key, value) -> {
                if (indexedPagesLemmas.containsKey(key)) {
                    indexedPagesLemmas.put(key, indexedPagesLemmas.get(key) + 1);
                } else {
                    indexedPagesLemmas.put(key, 1);
                }
            });
            lemmaCounting(indexedPageMap);
            mainLogger.warn("End parsing \t" + page.getRelPath());
        }
//        mainLogger.info(lemmaMap.size());
        indexedPagesLemmas.forEach((key, value) -> {
            Lemma lemma = new Lemma(key, value);
//            lemma.setSite(site);
            CompletableFuture<Lemma> test = mainService.getLemmaService().saveLemma(lemma);
            mainService.getLemmaService().findLemma(lemma.getLemma()).ifPresentOrElse(l->l.setSite(site),()->mainService.getLemmaService().saveLemma(lemma));

            mainLogger.info(test.join());
        });
        return indexedPagesLemmas;
    }

    public Map<String, Lemma> countLemmasOnPage(@NotNull Page indexingPage) {
        Map<String, Lemma> lemmas = new TreeMap<>();
        if (indexingPage.getContent() != null) {
            Document doc = Jsoup.parse(indexingPage.getContent());
            try {

                for (Field field : mainService.getFieldService().getAllFields()) {
                    LemmaCounter lemmaCounter = new LemmaCounter(doc.getElementsByTag(field.getName()).text());
                    lemmaCounter.countLemmas().forEach((key, value) -> {
                        lemmas.merge(key, new Lemma(key, value), Lemma::sum);
                    });
                }
            } catch (IOException e) {
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
        long startTime = System.currentTimeMillis();
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
        long endTime = System.currentTimeMillis();
        mainLogger.info("lemmas rank\t {} {}ms", lemmas.size(), endTime - startTime);
        return lemmas;
    }

    private void createSearchSiteIndexes(Site site) {
        for (Page page : mainService.getSiteService().findPageBySite(site)) {
            mainLogger.info("Start parsing \t" + page.getRelPath());
            Map<String, Float> indexedPageMap = startIndexingLemmasOnPage(page);
            indexedPageMap.forEach((key, value) -> {
                Lemma findLemma = mainService.getLemmaService().findLemma(key).get();
                mainLogger.info(findLemma);
                mainService.getSearchIndexService().saveIndex(new SearchIndex(page, findLemma, value));
            });
            mainLogger.warn(page.getRelPath() + " indexing is complete");
            mainLogger.warn("End parsing \t" + page.getRelPath());
        }
    }

    private void startSiteIndex(Site site) {

    }


    public void startIndexingSites(boolean isAllSite, @RequestParam Integer siteId) {
        if (isAllSite) {
            mainService.getSiteService().getAllSites().parallelStream().forEach(this::startSiteIndex);
            mainLogger.info("SITE PARSING IS FINISHED!");
        } else {
            Site findSite = mainService.getSiteService().getSite(siteId);
            startSiteIndex(findSite);
        }
    }


    public void startSiteParse(Site site) throws IOException {
        Link rootLink = new Link(site.getUrl());

        mainService.getSiteService().updateSite(site);
        RecursiveTask<Link> forkJoinTask = new SiteParser(rootLink, mainService, site);
        siteMapPool.invoke(forkJoinTask);
        insertToDatabase(rootLink);
    }


    private void insertToDatabase(Link link) {
        Page root = new Page(link);
//        root.setSite(link.getSite());
        Page page = mainService.getPageService().savePage(root).join();
        page.setSite(site);
        mainLogger.info(root);
        synchronized (site) {
            mainService.getPageService().updatePage(page);
        }
        link.getChildren().parallelStream().forEach(this::insertToDatabase);
    }
}
