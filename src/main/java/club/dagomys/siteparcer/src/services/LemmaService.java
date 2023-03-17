package club.dagomys.siteparcer.src.services;

import club.dagomys.siteparcer.src.entity.Lemma;
import club.dagomys.siteparcer.src.entity.Site;
import club.dagomys.siteparcer.src.repos.LemmaRepository;
import club.dagomys.siteparcer.src.repos.SiteRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class LemmaService {
    private final Logger mainLogger = LogManager.getLogger(LemmaService.class);

    @Autowired
    private LemmaRepository lemmaRepository;

    @Autowired
    private SiteRepository siteRepository;

    public List<Lemma> getAllLemma() {
        return new ArrayList<>(lemmaRepository.findAll());
    }

    public void saveAllLemma(Set<Lemma> lemmaList) {
        lemmaRepository.saveAll(lemmaList);
    }

    public Lemma saveLemma(Lemma lemma) {
        return lemmaRepository.saveAndFlush(lemma);
    }

    //доработать метод. Замедляет выполнение при обновлении лемм
    public Lemma saveOrUpdate(Lemma lemma) {
        Optional<Lemma> findLemma = lemmaRepository.findByLemmaAndSite(lemma.getLemma(), lemma.getSite());
        if (findLemma.isEmpty()) {
            mainLogger.info("saving lemma " + lemma);
            return saveLemma(lemma);
        } else {
            Lemma l = findLemma.get();
            l.setLemma(lemma.getLemma());
            l.setFrequency(lemma.getFrequency());
            mainLogger.info("update lemma " + l);
            return saveLemma(l);
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

    public Optional<Lemma> findLemma(String lemma) {
        return lemmaRepository.findAll().stream().filter(l -> l.getLemma().equalsIgnoreCase(lemma)).findFirst();
    }
    public Optional<List<Lemma>> findLemmas(String lemma) {
        return lemmaRepository.findAllByLemma(lemma);
    }
    public Optional<List<Lemma>> getLemmaList(Site site) {
        return lemmaRepository.getLemmaBySite(site);
    }

    public void deleteAllBySite(Site site) {
        lemmaRepository.deleteLemmaBySite(site);
    }


}
