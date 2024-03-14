package club.dagomys.siteparser.src.utils.siteparser;

import club.dagomys.siteparser.src.dto.FieldSelector;
import club.dagomys.siteparser.src.dto.Link;
import club.dagomys.siteparser.src.entity.*;
import club.dagomys.siteparser.src.exception.LemmaNotFoundException;
import club.dagomys.siteparser.src.exception.SiteIndexingException;
import club.dagomys.siteparser.src.services.IndexingService;
import club.dagomys.siteparser.src.utils.lemmatizator.LemmaCounter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.RecursiveTask;

@Component
@Slf4j
public class SiteParserRunner implements Runnable {
    private final Site site;
    private volatile boolean isRunning;
    @Autowired
    private final IndexingService indexingService;
    private final Field title;
    private final Field body;


    public SiteParserRunner(Site site, IndexingService indexingService) {
        this.indexingService = indexingService;
        this.site = site;
        title = indexingService.getFieldRepository().findFieldBySelector(FieldSelector.TITLE).get();
        body = indexingService.getFieldRepository().findFieldBySelector(FieldSelector.BODY).get();
    }


    @Override
    public void run() {
        this.isRunning = true;
        site.setStatus(SiteStatus.INDEXING);
        site.setStatusTime(LocalDateTime.now());
        indexingService.getSiteRepository().saveAndFlush(this.site);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yy HH:mm:ss");
        Calendar startTime = Calendar.getInstance();
        log.warn("start time " + dateFormat.format(startTime.getTime()));
        while (isRunning) {
            try {
                Link siteLinks = getSiteLinks();
                saveToDatabase(siteLinks);
                countLemmaFrequency();
                createSearchSiteIndexes();
                this.isRunning = false;
                Calendar finishDate = Calendar.getInstance();
                log.warn("finish time " + dateFormat.format(finishDate.getTime()));
                Duration duration = Duration.between(startTime.toInstant(), finishDate.toInstant());
                log.info("Duration " + duration.toString());
                site.setStatusTime(LocalDateTime.now());
                site.setStatus(SiteStatus.INDEXED);
                indexingService.getSiteRepository().saveAndFlush(this.site);
            } catch (SiteIndexingException | LemmaNotFoundException | IOException e) {
                this.isRunning = false;
                this.site.setStatus(SiteStatus.FAILED);
                this.site.setLastError(e.getMessage());
                log.error(e.getMessage() + " " + "Поток был прерван пользователем");
                indexingService.getSiteRepository().saveAndFlush(this.site);
            }

        }


    }


    private void countLemmaFrequency() throws SiteIndexingException, IOException {
        Map<String, Integer> indexedPagesLemmas = new TreeMap<>();
        for (Page page : indexingService.getPageRepository().findAllPageBySite(this.site).get()) {
            Map<String, Lemma> indexedPageMap = countLemmasOnPage(page);
            if (indexingService.getIsIndexing().get()) {
                log.info("Start parsing \t" + page.getRelPath());
                indexedPageMap.forEach((key, value) -> {
                    if (indexedPagesLemmas.containsKey(key)) {
                        indexedPagesLemmas.put(key, indexedPagesLemmas.get(key) + 1);
                    } else {
                        indexedPagesLemmas.put(key, 1);
                    }
                });
            } else throw new SiteIndexingException("Нахождение частоты лемм на сайте остановлено");
            lemmaCounting(indexedPageMap);
            log.warn("End parsing \t" + page.getRelPath());
        }
        saveLemmaToDatabase(indexedPagesLemmas);
    }

    private void saveLemmaToDatabase(Map<String, Integer> lemmaMap) {
        Set<Lemma> lemmaList = new TreeSet<>();
        lemmaMap.forEach((key, value) -> {
            try {
                if (indexingService.getIsIndexing().get()) {
                    Lemma lemma = new Lemma(key, value);
                    lemma.setSite(site);
                    lemmaList.add(lemma);
                } else throw new SiteIndexingException("Сохранение лемм в БД остановлено для сайта " + site.getUrl());
            } catch (SiteIndexingException e) {
                log.error(e.getMessage());
            }
        });
        indexingService.getLemmaRepository().deleteLemmaBySite(site);
        indexingService.getLemmaRepository().saveAll(lemmaList);
    }

    private Map<String, Lemma> countLemmasOnPage(@NotNull Page indexingPage) throws SiteIndexingException, IOException {
        Map<String, Lemma> lemmas = new TreeMap<>();
        if (indexingPage.getContent() != null) {
            Document doc = Jsoup.parse(indexingPage.getContent());
            if (indexingService.getIsIndexing().get()) {
                for (Field field : indexingService.getFieldRepository().findAll()) {
                    LemmaCounter lemmaCounter = new LemmaCounter(doc.getElementsByTag(field.getName()).text());
                    lemmaCounter.countLemmas().forEach((key, value) -> lemmas.merge(key, new Lemma(key, value), Lemma::sum));
                }
            } else throw new SiteIndexingException("Остановка парсинга страницы");
        } else {
            log.warn(indexingPage + " is not available. Code " + indexingPage.getStatusCode());
        }
        log.info("Lemmas count: {} {} ", lemmas.size(), "words");
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
            log.warn(indexingPage + " is not available. Code " + indexingPage.getStatusCode());
        }

        log.info("lemmas size\t {}", lemmas.size());
        return lemmas;

    }

    private void createSearchSiteIndexes() throws SiteIndexingException, LemmaNotFoundException {
        List<Lemma> lemmas = indexingService.getLemmaRepository().findAllLemmaBySite(site).orElseThrow();
        List<SearchIndex> searchIndexList = new ArrayList<>();
        for (Page page : indexingService.getPageRepository().findAllPageBySite(site).get()) {
            indexingService.getSearchIndexRepository().deleteByPage(page);
            long startTime = System.currentTimeMillis();
            log.info("Start parsing \t" + this.site.getUrl() + page.getRelPath());
            if (indexingService.getIsIndexing().get()) {
                Map<String, Float> indexedPageMap = startIndexingLemmasOnPage(page);
                indexedPageMap.forEach((key, value) -> {
                    Lemma findLemma = lemmas.parallelStream().filter(l -> l.getLemma().equalsIgnoreCase(key)).findFirst().orElseThrow();
                    searchIndexList.add(new SearchIndex(page, findLemma, value));
                });
            } else {
                throw new SiteIndexingException("Индексация остановлена на странице " + page.getRelPath() + " сайт " + page.getSite().getUrl());
            }
            long endTime = System.currentTimeMillis();
            log.warn(page.getRelPath() + " indexing is complete");
            log.warn("End parsing {} ms \t" + page.getRelPath(), endTime - startTime);
        }
        indexingService.getSearchIndexRepository().saveAll(searchIndexList);
    }


    private Link getSiteLinks() throws SiteIndexingException {
        Link rootLink = new Link(this.site.getUrl());
        indexingService.getSiteRepository().saveAndFlush(this.site);
        RecursiveTask<Link> forkJoinTask = new SiteParserTask(rootLink, indexingService, this.site);
        return indexingService.getForkJoinPool().invoke(forkJoinTask);
    }


    private void saveToDatabase(Link link) throws SiteIndexingException {
        if (link.getHtml() != null) {
            if (indexingService.getIsIndexing().get()) {
                Optional<Page> root = indexingService.getPageRepository().findByRelPathAndSite(link.getRelUrl(), link.getSite());
                if (root.isPresent()) {
                    indexingService.getPageRepository().saveAndFlush(root.get());
                } else {
                    indexingService.getPageRepository().saveAndFlush(new Page(link));
                }
            } else {
                throw new SiteIndexingException("Не удалось сохранить страницу " + link.getValue());
            }
        }
        for (Link l : link.getChildren()) {

            saveToDatabase(l);

        }

    }

}
