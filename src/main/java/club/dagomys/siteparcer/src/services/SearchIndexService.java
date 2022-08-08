package club.dagomys.siteparcer.src.services;

import club.dagomys.lemmatisator.scr.LemmaCounter;
import club.dagomys.siteparcer.src.entity.*;
import club.dagomys.siteparcer.src.repos.SearchIndexRepository;
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
    private Logger mainLogger = MainLog4jLogger.getInstance();

    @Autowired
    private SearchIndexRepository searchIndexRepository;

    @Autowired
    private LemmaService lemmaService;

    @Autowired
    private PageService pageService;

    @Autowired
    private FieldService fieldService;

    public SearchIndex saveIndex(SearchIndex searchIndex) {
        return searchIndexRepository.save(searchIndex);
    }

    public Map<String, Lemma> indexingAllPages() {
        Map<String, Integer> indexedPagesLemmas = new TreeMap<>();
        Map<String, Lemma> lemmaMap = new TreeMap<>();
        for (Page page : pageService.getAllPages()) {
            if (page.getContent() != null) {
                mainLogger.info("Start parsing \t" + page.getRelPath());
                Map<String, Lemma> indexedPageMap = startIndexingPage(page);
                indexedPageMap.forEach((value, key) -> {
                    if (indexedPagesLemmas.containsKey(value)) {
                        lemmaMap.put(value, new Lemma(value, indexedPagesLemmas.get(value) + 1));
                        indexedPagesLemmas.put(value, indexedPagesLemmas.get(value) + 1);
                    } else {
                        lemmaMap.put(value, new Lemma(value, 1));
                        indexedPagesLemmas.put(value, 1);

                    }
                });
                mainLogger.warn("End parsing \t" + page.getRelPath());
            } else {
                mainLogger.warn(page + " is not available. Code " + page.getStatusCode());
            }
        }
        indexedPagesLemmas.forEach((key, value) -> lemmaService.saveLemma(new Lemma(key, value)));
        mainLogger.info("Count of lemas " + lemmaMap.size());
        lemmaMap.entrySet().forEach(System.out::println);
        mainLogger.warn(indexedPagesLemmas);
        return lemmaMap;
    }


    public Map<String, Lemma> startIndexingPage(@NotNull Page indexingPage) {
        Document doc = Jsoup.parse(indexingPage.getContent());
        Map<String, Lemma> lemmas = new TreeMap<>();
        Map<String, Lemma> lemmaMap = new TreeMap<>();
        try {
            LemmaCounter lemmaCounter = new LemmaCounter();
            for (Field field : fieldService.getAllFields()) {
                lemmaCounter.countLemmas(doc.getElementsByTag(field.getName()).text()).forEach((key, value) -> {
                    lemmas.merge(key, new Lemma(key, value), Lemma::sum);
                });
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
//        lemmas.forEach((key, value)->lemmaMap.put(key, ));
        mainLogger.info(lemmas);
        return lemmaMap;
    }


    private float rankCounting(Lemma lemma) {
        return fieldService.getFieldByName(FieldSelector.TITLE).getWeight() * lemma.getFrequency() + fieldService.getFieldByName(FieldSelector.BODY).getWeight() * lemma.getFrequency();
    }
}
