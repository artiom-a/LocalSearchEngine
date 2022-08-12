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

    public SearchIndex saveIndex(SearchIndex searchIndex) {
        return searchIndexRepository.save(searchIndex);
    }


    public Map<String, Integer> countLemmasOnPage(Page indexingPage) {
        Map<String, Integer> lemmaCountMap = new TreeMap<>();

        Document doc = Jsoup.parse(indexingPage.getContent());

        try {
            LemmaCounter counter = new LemmaCounter();
            lemmaCountMap.putAll(counter.countLemmas(doc.text()));

        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        lemmaCountMap.entrySet().forEach(System.out::println);
        return lemmaCountMap;
    }

}
