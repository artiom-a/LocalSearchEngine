package club.dagomys.siteparcer.src.services;

import club.dagomys.siteparcer.src.entity.*;
import club.dagomys.siteparcer.src.repos.SearchIndexRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
public class SearchIndexService {
    private final Logger mainLogger = LogManager.getLogger(SearchIndex.class);

    @Autowired
    private SearchIndexRepository searchIndexRepository;


    @Async
    public CompletableFuture<SearchIndex> saveIndex(SearchIndex searchIndex) {
        return CompletableFuture.completedFuture(searchIndexRepository.saveAndFlush(searchIndex));
    }

    public void saveAllIndexes(List<SearchIndex> indexes) {
        searchIndexRepository.saveAll(indexes);
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
        return searchIndexRepository.findByPageAndLemma(page, lemma).orElseThrow(() ->
                new EntityNotFoundException("SearchIndex object is not found " + page.getRelPath() + "\t" + lemma.getLemma()));
    }
}
