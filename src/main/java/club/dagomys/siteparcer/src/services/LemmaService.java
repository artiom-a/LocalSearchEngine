package club.dagomys.siteparcer.src.services;

import club.dagomys.siteparcer.src.entity.Lemma;
import club.dagomys.siteparcer.src.repos.LemmaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.CriteriaBuilder;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class LemmaService {
    @Autowired
    private LemmaRepository lemmaRepository;

    public List<Lemma> gelAllLemma() {
        return new ArrayList<>(lemmaRepository.findAll());
    }

    public void saveLemma(Lemma lemma) {
            lemmaRepository.save(lemma);
    }

    public Iterable<Lemma> saveAllLemmas(Set<Lemma> lemmas) {
        return lemmaRepository.saveAllAndFlush(lemmas);
    }

    public void deleteAllLemma(){
        lemmaRepository.deleteAll();
    }
}
