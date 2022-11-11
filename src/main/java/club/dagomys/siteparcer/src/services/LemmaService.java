package club.dagomys.siteparcer.src.services;

import club.dagomys.lemmatisator.scr.LemmaCounter;
import club.dagomys.siteparcer.src.entity.Field;
import club.dagomys.siteparcer.src.entity.Lemma;
import club.dagomys.siteparcer.src.entity.Page;
import club.dagomys.siteparcer.src.repos.LemmaRepository;
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
public class LemmaService {
    private final Logger mainLogger = LogManager.getLogger(LemmaService.class);

    @Autowired
    private LemmaRepository lemmaRepository;

    @Autowired
    private PageService pageService;

    @Autowired
    private FieldService fieldService;

    public List<Lemma> gelAllLemma() {
        return new ArrayList<>(lemmaRepository.findAll());
    }

    public void saveAllLemma(Set<Lemma> lemmaList) {
        lemmaRepository.saveAll(lemmaList);
    }

    public void saveLemma(Lemma lemma) {
        lemmaRepository.save(lemma);
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

}
