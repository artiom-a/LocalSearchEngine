package club.dagomys.siteparcer.src.services;

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
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class SiteParserRunner implements Runnable {
    private final Logger mainLogger = LogManager.getLogger(SiteParserRunner.class);
    private final int CORE_COUNT = Runtime.getRuntime().availableProcessors();
    private final ForkJoinPool siteMapPool = new ForkJoinPool(CORE_COUNT);
    private final Site site;
    private AtomicBoolean isStarted = new AtomicBoolean(false);
    private volatile boolean doStop = false;

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
        site.setStatus(SiteStatus.INDEXING);
        site.setStatusTime(LocalDateTime.now());
        mainService.getSiteService().saveOrUpdate(this.site);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yy HH:mm:ss");
        Calendar startTime = Calendar.getInstance();
        mainLogger.warn("start time " + dateFormat.format(startTime.getTime()));
        while (isRunning()) {
            try {
                Link siteLinks = getSiteLinks();
                insertToDatabase(siteLinks);
                Map<String, Integer> lemmaFrequencyMap = countLemmaFrequency();
                saveLemmaToDatabase(lemmaFrequencyMap);

                createSearchSiteIndexes();
            } catch (Exception ex) {
//            mainLogger.error(ex);
                mainLogger.error(ex.getMessage());
            }
            doStop();
        }

        Calendar finishDate = Calendar.getInstance();
        mainLogger.warn("finish time " + dateFormat.format(finishDate.getTime()));
        Duration duration = Duration.between(startTime.toInstant(), finishDate.toInstant());
        mainLogger.info("Duration " + duration.toString());
        site.setStatusTime(LocalDateTime.now());
        site.setStatus(SiteStatus.INDEXED);
        mainService.getSiteService().saveOrUpdate(this.site);
    }


    private Map<String, Integer> countLemmaFrequency() {
        Map<String, Integer> indexedPagesLemmas = new TreeMap<>();
        for (Page page : mainService.getPageService().getPagesBySite(this.site)) {
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

    private Iterable<Lemma> saveLemmaToDatabase(Map<String, Integer> lemmaMap) {
        Set<Lemma> lemmaList = new TreeSet<>();
        lemmaMap.forEach((key, value) -> {
            Lemma lemma = new Lemma(key, value);
            lemma.setSite(site);
            lemmaList.add(lemma);
        });
        mainService.getLemmaService().deleteAllBySite(site);
        return mainService.getLemmaService().saveAllLemmas(lemmaList);
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

    private void createSearchSiteIndexes() {
        List<Lemma> lemmas = mainService.getLemmaService().getLemmaList(site).orElseThrow();
        for (Page page : mainService.getPageService().getPagesBySite(this.site)) {
            long startTime = System.currentTimeMillis();
            mainLogger.info("Start parsing \t" + this.site.getUrl() + "" + page.getRelPath());
            Map<String, Float> indexedPageMap = startIndexingLemmasOnPage(page);
            indexedPageMap.forEach((key, value) -> {
                Lemma findLemma = lemmas.stream().filter(l -> l.getLemma().equalsIgnoreCase(key)).findFirst().orElseThrow();
//                Lemma findLemma = mainService.getLemmaService().findLemma(key).get();

                mainService.getSearchIndexService().saveIndex(new SearchIndex(page, findLemma, value));
            });
            long endTime = System.currentTimeMillis();
            mainLogger.warn(page.getRelPath() + " indexing is complete");
            mainLogger.warn("End parsing {} ms \t" + page.getRelPath(), endTime - startTime);
        }
    }


    public Link getSiteLinks() throws IOException {
        Link rootLink = new Link(this.site.getUrl());
        mainService.getSiteService().saveSite(this.site);
        RecursiveTask<Link> forkJoinTask = new SiteParser(rootLink, mainService, this.site);
        return siteMapPool.invoke(forkJoinTask);
    }


    public void insertToDatabase(Link link) {
        Page root = new Page(link);
        link.getChildren().forEach(this::insertToDatabase);
        mainService.getPageService().saveOrUpdate(root);
    }

    public synchronized void doStop() {
        this.doStop = true;
    }

    private synchronized boolean isRunning() {
        return !this.doStop;
    }
}
