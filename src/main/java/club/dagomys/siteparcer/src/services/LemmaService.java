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

    private final Map<String, Lemma> lemmaMap = new TreeMap<>();

    public Map<String, Integer> lemmaFrequencyCounter() {
        Map<String, Integer> indexedPagesLemmas = new TreeMap<>();
        for (Page page : pageService.getAllPages()) {
            mainLogger.info("Start parsing \t" + page.getRelPath());
            Map<String, Lemma> indexedPageMap = startCountingLemmasOnPage(page);
            indexedPageMap.forEach((key, value) -> {
                if (indexedPagesLemmas.containsKey(key)) {
                    indexedPagesLemmas.put(key, indexedPagesLemmas.get(key) + 1);
                } else {
                    indexedPagesLemmas.put(key, 1);
                }
            });
            lemmaCounting(indexedPageMap);
            mainLogger.warn("End parsing \t" + page.getRelPath());
        }
        mainLogger.info(lemmaMap.size());
        indexedPagesLemmas.forEach((key, value) -> saveLemma(new Lemma(key, value)));
        return indexedPagesLemmas;
    }

    public Map<String, Lemma> startCountingLemmasOnPage(@NotNull Page indexingPage) {
        Map<String, Lemma> lemmas = new TreeMap<>();
        if (indexingPage.getContent() != null) {
            Document doc = Jsoup.parse(indexingPage.getContent());
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
        } else {
            mainLogger.warn(indexingPage + " is not available. Code " + indexingPage.getStatusCode());
        }
        mainLogger.info(lemmas);
        return lemmas;
    }


    private void lemmaCounting(Map<String, Lemma> inputMap) {
        inputMap.forEach((key, value) -> {
            if (lemmaMap.containsKey(key)) {
                lemmaMap.put(key, new Lemma(key, lemmaMap.get(key).getFrequency() + value.getFrequency()));
            } else {
                lemmaMap.put(key, value);
            }
        });
    }
}
