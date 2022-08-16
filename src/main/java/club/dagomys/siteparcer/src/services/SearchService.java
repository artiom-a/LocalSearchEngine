package club.dagomys.siteparcer.src.services;

import club.dagomys.lemmatisator.scr.LemmaCounter;
import club.dagomys.siteparcer.src.entity.Lemma;
import club.dagomys.siteparcer.src.entity.Page;
import club.dagomys.siteparcer.src.entity.SearchIndex;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.filter.ShallowEtagHeaderFilter;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SearchService {
    private final Logger mainLogger = LogManager.getLogger(SearchService.class);

    @Autowired
    private LemmaService lemmaService;

    @Autowired
    private PageService pageService;

    @Autowired
    SearchIndexService searchIndexService;

    public List<Lemma> search(String searchLine) {
        SortedSet<Lemma> findLemmas = new TreeSet<>();
        List<Lemma> sortedList = new ArrayList<>();
        try {
            LemmaCounter counter = new LemmaCounter();
            Set<String> lemmas = counter.getLemmaSet(searchLine);
            lemmas.forEach(l -> {
                Optional<Lemma> findLemma = lemmaService.findLemma(l);
                findLemma.ifPresentOrElse(findLemmas::add, () -> System.out.println("value is not present"));
            });

            deleteCommonLemmas(findLemmas);
           sortedList = new ArrayList<>(sortLemmasByFrequency(findLemmas));
            mainLogger.info(findLemmas);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sortedList;
    }

    private Set<Lemma> deleteCommonLemmas(Set<Lemma> lemmas) {
        final double percent = 0.98;
        double frequency = pageService.getAllPages().size() * percent;
        lemmas.removeIf(l -> l.getFrequency() > frequency);
        return lemmas;
    }

    private List<Lemma> sortLemmasByFrequency(Set<Lemma> lemmaSet) {
        return lemmaSet.stream().sorted(Comparator.comparing(Lemma::getFrequency)).collect(Collectors.toCollection(ArrayList::new));
    }


    public List<SearchIndex> findIndexedPage(List<Lemma> lemmaList) {
        List<SearchIndex> indexList = new ArrayList<>();
        indexList = searchIndexService.findIndexByLemma(lemmaList.stream().findFirst().get());

        return indexList;
    }
}
