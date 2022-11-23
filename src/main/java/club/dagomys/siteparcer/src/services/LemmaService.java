package club.dagomys.siteparcer.src.services;

import club.dagomys.siteparcer.src.entity.Lemma;
import club.dagomys.siteparcer.src.repos.LemmaRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
public class LemmaService {
    private final Logger mainLogger = LogManager.getLogger(LemmaService.class);

    @Autowired
    private LemmaRepository lemmaRepository;

    @Autowired
    private PageService pageService;

    @Autowired
    private FieldService fieldService;

    public List<Lemma> getAllLemma() {
        return new ArrayList<>(lemmaRepository.findAll());
    }

    public void saveAllLemma(Set<Lemma> lemmaList) {
        lemmaRepository.saveAll(lemmaList);
    }

//    @Async
    public CompletableFuture<Lemma> saveLemma(Lemma lemma) {
        return CompletableFuture.completedFuture(lemmaRepository.save(lemma));
    }

    public Iterable<Lemma> saveAllLemmas(Set<Lemma> lemmas) {
        return lemmaRepository.saveAllAndFlush(lemmas);
    }

    public void deleteAllLemma() {
        lemmaRepository.deleteAll();
    }

    public Optional<Lemma> findLemma(String lemma) {
        return lemmaRepository.findAll().stream().filter(l -> l.getLemma().equalsIgnoreCase(lemma)).findFirst();
    }


//    public CompletableFuture<Optional<Lemma>> asyncLemmaFind(String lemma) {
//        return lemmaRepository.findAll().stream().filter(l -> l.getLemma().equalsIgnoreCase(lemma)).findFirst();
//    }

}
