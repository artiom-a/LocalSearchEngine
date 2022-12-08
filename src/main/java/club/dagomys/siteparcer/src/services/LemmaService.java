package club.dagomys.siteparcer.src.services;

import club.dagomys.siteparcer.src.entity.Lemma;
import club.dagomys.siteparcer.src.entity.Page;
import club.dagomys.siteparcer.src.entity.Site;
import club.dagomys.siteparcer.src.repos.LemmaRepository;
import club.dagomys.siteparcer.src.repos.SiteRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
@Transactional
public class LemmaService {
    private final Logger mainLogger = LogManager.getLogger(LemmaService.class);

    @Autowired
    private LemmaRepository lemmaRepository;

    @Autowired
    private PageService pageService;

    @Autowired
    private FieldService fieldService;

    @Autowired
    private SiteRepository siteRepository;

    public List<Lemma> getAllLemma() {
        return new ArrayList<>(lemmaRepository.findAll());
    }

    public void saveAllLemma(Set<Lemma> lemmaList) {
        lemmaRepository.saveAll(lemmaList);
    }

    @Async("taskExecutor")
    @Transactional
    public CompletableFuture<Lemma> saveLemma(Lemma lemma) {
        CompletableFuture<Lemma> completableFuture = new CompletableFuture<>();
        completableFuture.complete(lemmaRepository.saveAndFlush(lemma));
        return completableFuture;
    }

    @Async("taskExecutor")
    @Transactional
    public void update(Lemma lemma) {
        Optional<Lemma> findLemma = lemmaRepository.findByLemmaAndSite(lemma.getLemma(), lemma.getSite());
        Optional<Site> findSite = siteRepository.findById(lemma.getSite().getId());
        if (findLemma.isEmpty()) {
            mainLogger.info("saving page " + lemma);
            lemma.setSite(findSite.get());
            lemmaRepository.saveAndFlush(lemma);
        } else {
            Lemma l = findLemma.get();
            l.setLemma(lemma.getLemma());
            l.setFrequency(lemma.getFrequency());
            l.setSite(lemma.getSite());
            mainLogger.info("update page " + l);
            lemmaRepository.saveAndFlush(l);
        }
    }

    public Iterable<Lemma> saveAllLemmas(Set<Lemma> lemmas) {
        return lemmaRepository.saveAllAndFlush(lemmas);
    }

    public void deleteLemma(Lemma lemma) {
        lemmaRepository.delete(lemma);
    }

    public void deleteAllLemma() {
        lemmaRepository.deleteAll();
    }
    @Async("taskExecutor")
    @Transactional
    public CompletableFuture<Optional<Lemma>> findLemma(String lemma) {
        CompletableFuture<Optional<Lemma>> completableFuture = new CompletableFuture<>();
        completableFuture.complete(lemmaRepository.findAll().parallelStream().filter(l -> l.getLemma().equalsIgnoreCase(lemma)).findFirst());
        return completableFuture;
    }

    @Async("taskExecutor")
    @Transactional
    public void deleteAllBySite(Site site){
        lemmaRepository.deleteAllBySite(site);
    }

//    public CompletableFuture<Optional<Lemma>> asyncLemmaFind(String lemma) {
//        return lemmaRepository.findAll().stream().filter(l -> l.getLemma().equalsIgnoreCase(lemma)).findFirst();
//    }

}
