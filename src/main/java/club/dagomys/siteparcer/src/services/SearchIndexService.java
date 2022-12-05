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
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

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

    @Autowired
    private SiteService siteService;

    @Async("taskExecutor")
    public CompletableFuture<SearchIndex> saveIndex(SearchIndex searchIndex) {
        return CompletableFuture.completedFuture(searchIndexRepository.save(searchIndex));
    }

    public List<SearchIndex> getAllIndexes() {
        List<SearchIndex> indexList = new ArrayList<>();
        indexList.addAll(searchIndexRepository.findAll());
        return indexList;
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
