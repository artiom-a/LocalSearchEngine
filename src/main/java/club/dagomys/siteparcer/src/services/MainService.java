package club.dagomys.siteparcer.src.services;

import club.dagomys.lemmatisator.scr.LemmaCounter;
import club.dagomys.siteparcer.src.entity.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;

@Service
public class MainService extends Thread {
    private final Logger mainLogger = LogManager.getLogger(MainService.class);
    private final int CORE_COUNT = Runtime.getRuntime().availableProcessors();
    private final ThreadPoolExecutor threadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(CORE_COUNT);
    private final ForkJoinPool siteMapPool = new ForkJoinPool(Runtime.getRuntime().availableProcessors());

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


    private Map<String, Integer> countLemmaFrequency(Site site) {
        Map<String, Integer> indexedPagesLemmas = new TreeMap<>();
        for (Page page : pageService.getPagesBySite(site)) {
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
            lemma.setSite(site);
            lemmaService.saveLemma(lemma);
        });
        return indexedPagesLemmas;
    }

    public Map<String, Lemma> countLemmasOnPage(@NotNull Page indexingPage) {
        Map<String, Lemma> lemmas = new TreeMap<>();
        if (indexingPage.getContent() != null) {
            Document doc = Jsoup.parse(indexingPage.getContent());
            try {

                for (Field field : fieldService.getAllFields()) {
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
        Field title = fieldService.getFieldByName(FieldSelector.TITLE);
        Field body = fieldService.getFieldByName(FieldSelector.BODY);
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
    private void insertToDatabase(Link link) {
        Page root = new Page(link);
        pageService.updatePage(root);
        link.getChildren().forEach(this::insertToDatabase);
    }
    private void saveSearchIndexes(Site site) {
        for (Page page : siteService.findPageBySite(site)) {
            mainLogger.info("Start parsing \t" + page.getRelPath());
            Map<String, Float> indexedPageMap = startIndexingLemmasOnPage(page);
            indexedPageMap.forEach((key, value) -> {
                Lemma findLemma = lemmaService.findLemma(key).get();
                synchronized (SearchIndex.class) {
                    searchIndexService.saveIndex(new SearchIndex(page, findLemma, value));
                }
            });
            mainLogger.warn(page.getRelPath() + " indexing is complete");
            mainLogger.warn("End parsing \t" + page.getRelPath());
        }
    }


    private void startSiteIndex(Site site) {

        try {
            startSiteParse(site);
        } catch (Exception ex) {
            mainLogger.error(ex.getMessage());
        }
        synchronized (Site.class) {
            countLemmaFrequency(site);
            saveSearchIndexes(site);
            site.setStatusTime(LocalDateTime.now());
            site.setStatus(SiteStatus.INDEXED);
            siteService.updateSite(site);
        }
    }


    public void startIndexingSites(boolean isAllSite, @RequestParam Integer siteId) {
        if (isAllSite) {
            siteService.getAllSites().parallelStream().forEach(this::startSiteIndex);
            mainLogger.info("SITE PARSING IS FINISHED!");
        } else {
            Site findSite = siteService.getSite(siteId);
            startSiteIndex(findSite);
        }
    }


    public void startSiteParse(Site site) throws IOException {
        site.setStatus(SiteStatus.INDEXING);
        site.setStatusTime(LocalDateTime.now());
        siteService.updateSite(site);

        Link rootLink = new Link(site.getUrl());
        mainLogger.info(rootLink);
        RecursiveTask<Link> forkJoinTask = new SiteParser(rootLink, this);
        siteMapPool.invoke(forkJoinTask);
        insertToDatabase(rootLink);
    }

//    private void insertToDatabase(Link link, Site site) {
//        link.getChildren().forEach(child ->
//                {
//                    insertToDatabase(child, site);
//                }
//        );
//    }

    private String createSitemap(Link node) {
        String tabs = String.join("", Collections.nCopies(node.getLayer(), "\t"));
        StringBuilder result = new StringBuilder(tabs + node.getRelUrl());
        node.getChildren().forEach(child -> {
            result.append("\n").append(createSitemap(child));
        });
        return result.toString();
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

    @Override
    public void run() {
    }
}
