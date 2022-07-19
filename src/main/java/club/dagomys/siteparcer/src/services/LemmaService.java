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

    @Autowired
    private static Map<Lemma, Integer> lemmaMap = new TreeMap<>();
    private static ArrayList<Lemma> lemmaList = new ArrayList<>();

    public void saveLemma(Lemma lemma) {
        lemmaList.add(lemma);
    }

    public void saveAllLemmas(){
        System.out.println("Saving...");
        lemmaMap.forEach((key, value) -> lemmaRepository.save(new Lemma(key.getLemma(), value)));
        System.out.println("Saving complete!");
    }

    public Map<String, Integer> findByName() {
        return countDuplicates(lemmaList);
    }

    private static Map<String, Integer> countDuplicates(List<Lemma> inputList) {
        return inputList.stream().collect(Collectors.toMap(Lemma::getLemma, v -> 1, Integer::sum));
    }

    public Iterable<Lemma> saveAllLemmas(List<Lemma> lemmas) {
        return lemmaRepository.saveAllAndFlush(lemmas);
    }
}
