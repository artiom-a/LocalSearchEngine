package club.dagomys.siteparcer.src.services;

import club.dagomys.lemmatisator.scr.LemmaCounter;
import club.dagomys.siteparcer.src.entity.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

@Component
@Scope("prototype")
public class MainService {
    private final Map<String, Lemma> lemmaMap = new TreeMap<>();
    private final Logger mainLogger = LogManager.getLogger(MainService.class);
    private final int CORE_COUNT = Runtime.getRuntime().availableProcessors();
    private final ThreadPoolExecutor threadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(CORE_COUNT);

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
        mainLogger.info(lemmaMap.size());
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
        mainLogger.info(lemmas);
        return lemmas;
    }


    private void lemmaCounting(Map<String, Lemma> inputMap) {
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
        mainLogger.info("lemmas rank\t" + lemmas);
        return lemmas;
    }

    public void startIndexingSite(Site site) {
        startSiteParse(site);
        countLemmaFrequency(site);

        synchronized (site) {
            for (Page page : siteService.findPageBySite(site)) {
                mainLogger.info("Start parsing \t" + page.getRelPath());
                Map<String, Float> indexedPageMap = startIndexingLemmasOnPage(page);
                indexedPageMap.forEach((key, value) -> {
                    Lemma findLemma = lemmaService.findLemma(key).get();
                    searchIndexService.saveIndex(new SearchIndex(page, findLemma, value));
                });
                mainLogger.warn(page.getRelPath() + " indexing is complete");
                mainLogger.warn("End parsing \t" + page.getRelPath());
            }
        }
        site.setStatus(SiteStatus.INDEXED);
        siteService.saveSite(site);
        lemmaMap.clear();
    }

    public void startIndexingAllSites() {
        siteService.getAllSites().forEach((site -> {
            startSiteParse(site);
            countLemmaFrequency(site);

            for (Page page : siteService.findPageBySite(site)) {
                mainLogger.info("Start parsing \t" + page.getRelPath());
                Map<String, Float> indexedPageMap = startIndexingLemmasOnPage(page);
                indexedPageMap.forEach((key, value) -> {
                    Lemma findLemma = lemmaService.findLemma(key).get();
                    searchIndexService.saveIndex(new SearchIndex(page, findLemma, value));
                });
                mainLogger.warn(page.getRelPath() + " indexing is complete");
                mainLogger.warn("End parsing \t" + page.getRelPath());
            }
            site.setStatus(SiteStatus.INDEXED);
            siteService.saveSite(site);
        }));
//        threadPool.shutdown();
    }


    public Site startSiteParse(Site site) {

        if (!site.getUrl().endsWith("/")) {
            site.setUrl(site.getUrl().concat("/"));
        }
        SiteParserRunner siteParser = new SiteParserRunner(site, this);
        if (siteParser.isStarted()) {
            mainLogger.warn("SiteParser is running!");
        } else {
            site.setStatus(SiteStatus.INDEXING);
            site.setStatusTime(LocalDateTime.now());
            siteService.saveSite(site);
            siteParser.run();
        }
        return site;
    }


    public synchronized void insertToDatabase(Link link, Site site) {
        Page root = new Page(link.getRelUrl());
        root.setStatusCode(link.getStatusCode());
        root.setContent(link.getHtml());
        root.setSite(site);
        pageService.savePage(root);
        link.getChildren().forEach(child ->
                {
                    insertToDatabase(child, site);
                }
        );
    }

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
}
