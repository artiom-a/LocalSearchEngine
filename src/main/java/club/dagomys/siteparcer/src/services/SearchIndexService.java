package club.dagomys.siteparcer.src.services;

import club.dagomys.lemmatisator.scr.LemmaCounter;
import club.dagomys.siteparcer.src.entity.*;
import club.dagomys.siteparcer.src.repos.SearchIndexRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.io.IOException;
import java.util.*;

@Service
public class SearchIndexService {
    private final Logger mainLogger = LogManager.getLogger(SearchIndex.class);

    @Autowired
    private SearchIndexRepository searchIndexRepository;

    @Autowired
    private LemmaService lemmaService;

    @Autowired
    private FieldService fieldService;

    @Autowired
    private PageService pageService;

    public SearchIndex saveIndex(SearchIndex searchIndex) {
        return searchIndexRepository.save(searchIndex);
    }

    public List<SearchIndex> getAllIndexes() {
        List<SearchIndex> indexList = new ArrayList<>();
        indexList.addAll(searchIndexRepository.findAll());
        return indexList;
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
        mainLogger.info(lemmas);
        return lemmas;
    }

    public void startIndexingAllPages() {
        lemmaService.lemmaFrequencyCounter();
        for (Page page : pageService.getAllPages()) {
            mainLogger.info("Start parsing \t" + page.getRelPath());
            Map<String, Float> indexedPageMap = startIndexingLemmasOnPage(page);
            indexedPageMap.forEach((key, value) -> {
                Lemma findLemma = lemmaService.findLemma(key).get();
                saveIndex(new SearchIndex(page, findLemma, value));
            });
            mainLogger.warn(page.getRelPath() + " indexing is complete");
            mainLogger.warn("End parsing \t" + page.getRelPath());
        }
    }

    public List<SearchIndex> findIndexByLemma(Lemma lemma) {
        return searchIndexRepository.findByLemmaOrderByRankDesc(lemma);
    }

    public List<SearchIndex> findIndexByPage(Page page) {
        return searchIndexRepository.findByPage(page);
    }

    public SearchIndex findIndexByPageAndLemma(Page page, Lemma lemma) throws Throwable {
        return searchIndexRepository.findByPageAndLemma(page, lemma).orElseThrow(()->
                new EntityNotFoundException("SearchIndex object is not found " + page.getRelPath() + "\t" + lemma.getLemma()));
    }
}
