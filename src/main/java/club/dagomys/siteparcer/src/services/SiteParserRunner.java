package club.dagomys.siteparcer.src.services;

import club.dagomys.siteparcer.src.lemmatisator.LemmaCounter;
import club.dagomys.siteparcer.src.entity.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class SiteParserRunner extends Thread {
    private final Logger mainLogger = LogManager.getLogger(SiteParserRunner.class);
    private final int CORE_COUNT = Runtime.getRuntime().availableProcessors();
    private final ForkJoinPool siteMapPool = new ForkJoinPool(CORE_COUNT);
    private final Site site;
    private AtomicBoolean isStarted = new AtomicBoolean(false);


    @Autowired
    private final MainService mainService;

    @Autowired
    private ExecutorService asyncService;

    @Autowired
    public SiteParserRunner(Site site, MainService mainService) {
        this.mainService = mainService;
        this.site = site;
    }

    private void setStarted(boolean started) {
        isStarted = new AtomicBoolean(started);
    }

    public AtomicBoolean isStarted() {
        return isStarted;
    }

//    public void stop() {
//        setStarted(false);
//    }

    @Override
    public void run() {
        setStarted(true);
        site.setStatus(SiteStatus.INDEXING);
        site.setStatusTime(LocalDateTime.now());
        mainService.getSiteService().updateSite(site);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yy HH:mm:ss");
        Calendar startTime = Calendar.getInstance();
        mainLogger.warn("start time " + dateFormat.format(startTime.getTime()));
        try {
            Link siteLinks = getSiteLinks();
            insertToDatabase(siteLinks);

            Map<String, Integer> lemmaFrequencyMap = countLemmaFrequency();
            saveLemmaToDatabase(lemmaFrequencyMap);

            createSearchSiteIndexes();
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


    private Map<String, Integer> countLemmaFrequency() {
        Map<String, Integer> indexedPagesLemmas = new TreeMap<>();
        mainLogger.info(mainService.getPageService().getPagesBySite(site));
        for (Page page : mainService.getPageService().getPagesBySite(site).join()) {
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
        return indexedPagesLemmas;
    }

    private void saveLemmaToDatabase(Map<String, Integer> lemmaMap) {
        lemmaMap.forEach((key, value) -> {
            Lemma lemma = new Lemma(key, value);
            lemma.setSite(this.site);
            mainLogger.info(lemma);
            mainService.getLemmaService().saveLemma(lemma);
        });
    }

    public Map<String, Lemma> countLemmasOnPage(@NotNull Page indexingPage) {
        Map<String, Lemma> lemmas = new TreeMap<>();
        if (indexingPage.getContent() != null) {
            Document doc = Jsoup.parse(indexingPage.getContent());
            try {

                for (Field field : mainService.getFieldService().getAllFields()) {
                    LemmaCounter lemmaCounter = new LemmaCounter(doc.getElementsByTag(field.getName()).text());
                    lemmaCounter.countLemmas().forEach((key, value) -> lemmas.merge(key, new Lemma(key, value), Lemma::sum));
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

    private void createSearchSiteIndexes() {
        for (Page page : mainService.getPageService().getPagesBySite(site).join()) {
            mainLogger.info("Start parsing \t" + page.getRelPath());
            Map<String, Float> indexedPageMap = startIndexingLemmasOnPage(page);
            indexedPageMap.forEach((key, value) -> {
                Lemma findLemma = mainService.getLemmaService().findLemma(key).join().get();
//                mainLogger.info(findLemma);
                mainService.getSearchIndexService().saveIndex(new SearchIndex(page, findLemma, value));
            });
            mainLogger.warn(page.getRelPath() + " indexing is complete");
            mainLogger.warn("End parsing \t" + page.getRelPath());
        }
    }


    public Link getSiteLinks() throws IOException {
        Link rootLink = new Link(site.getUrl());
        mainService.getSiteService().updateSite(site);
        RecursiveTask<Link> forkJoinTask = new SiteParser(rootLink, mainService, site);
        return siteMapPool.invoke(forkJoinTask);
    }


    public void insertToDatabase(Link link) {
        Page root = new Page(link);
        synchronized (Site.class) {
            mainService.getPageService().updatePage(root);
        }
        link.getChildren().forEach(this::insertToDatabase);
    }
}
