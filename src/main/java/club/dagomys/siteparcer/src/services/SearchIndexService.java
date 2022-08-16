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

//    public List<SearchIndex> findIndexByLemma(Lemma lemma) {
//        List<SearchIndex> findIndexes = new ArrayList<>();
//        for (SearchIndex index : getAllIndexes()) {
//            if (index.getLemma().equals(lemma)) {
//                findIndexes.add(index);
//                return findIndexes;
//            }
//        }
//        return findIndexes;
//    }

    private Map<String, Float> startIndexingLemmasOnPage(@NotNull Page indexingPage) {
        Map<String, Float> lemmas = new TreeMap<>();
        Field title = fieldService.getFieldByName(FieldSelector.TITLE);
        Field body = fieldService.getFieldByName(FieldSelector.BODY);
        if (indexingPage.getContent() != null) {
            Document doc = Jsoup.parse(indexingPage.getContent());
            try {
                LemmaCounter lemmaCounter = new LemmaCounter();
                Map<String, Integer> titleLemmas = lemmaCounter.countLemmas(doc.getElementsByTag(title.getName()).text());
                Map<String, Integer> bodyLemmas = lemmaCounter.countLemmas(doc.getElementsByTag(body.getName()).text());
                bodyLemmas.forEach((key, value) -> {
                    if (titleLemmas.containsKey(key)) {
                        float R = title.getWeight() * titleLemmas.get(key) + value * body.getWeight();
                        lemmas.put(key, R);
//                        System.out.println(key + "\t" + R + " count " + value + " title count " + titleLemmas.get(key));
                    } else {
                        float R = bodyLemmas.get(key) * body.getWeight();
                        lemmas.put(key, R);
//                        System.out.println(key + "\t" + R + " count " + titleLemmas.get(key) + " body count " + value);
                    }
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
                mainLogger.warn(findLemma + " indexing is complete");
            });
            mainLogger.warn("End parsing \t" + page.getRelPath());
        }
    }

    public List<SearchIndex> findIndexByLemma(Lemma lemma) {
        return searchIndexRepository.findByLemma(lemma);
    }
}
