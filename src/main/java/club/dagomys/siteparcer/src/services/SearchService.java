package club.dagomys.siteparcer.src.services;

import club.dagomys.lemmatisator.scr.LemmaCounter;
import club.dagomys.siteparcer.src.entity.Lemma;
import club.dagomys.siteparcer.src.entity.Page;
import club.dagomys.siteparcer.src.entity.SearchIndex;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    public List<Page> search(String searchLine) {
        List<Lemma> findLemmas = new ArrayList<>(getLemmasFromRequest(searchLine));
        Lemma minFreqLemma = getMinLemma(findLemmas);
        List<Page> findPages = findIndexedPage(minFreqLemma);
        List<SearchIndex> indexingList = searchIndexService.findIndexByLemma(minFreqLemma);
        List<Page> pageIndexes = new ArrayList<>();
        indexingList.forEach(indexing -> pageIndexes.add(indexing.getPage()));

        for (Lemma lemma : findLemmas) {
            if (!pageIndexes.isEmpty() && lemma != minFreqLemma) {
                List<SearchIndex> secondIndexSearch = searchIndexService.findIndexByLemma(lemma);
                List<Page> tempList = new ArrayList<>();
                secondIndexSearch.forEach(indexing -> tempList.add(indexing.getPage()));
                findPages.retainAll(tempList);
            }
        }
        return getRelRelevance(findPages, findLemmas).keySet().stream().toList();
    }

    private float getAbsRelevance(Page page, List<Lemma> lemmas) {
        float r = 0f;
        for (Lemma lemma : lemmas) {
            SearchIndex searchIndex = null;
            try {
                searchIndex = searchIndexService.findIndexByPageAndLemma(page, lemma);
                r = r + searchIndex.getRank();
//                mainLogger.info(String.format("%.2f", r) + " \t" + searchIndex.getLemma() + " ->>> " + searchIndex.getRank());
            } catch (Throwable e) {
                mainLogger.error(e.getMessage());
            }

        }
        return r;
    }

    private Map<Page, Float> getRelRelevance(List<Page> pageList, List<Lemma> lemmaList) {
        float maxRel = 0f;
        Map<Page, Float> sortedMap = new LinkedHashMap<>();
        Map<Page, Float> pagesForRelevance = new LinkedHashMap<>();
        for (Page page : pageList) {
            float r = getAbsRelevance(page, lemmaList);
            pagesForRelevance.put(page, r);
            if (r > maxRel)
                maxRel = r;
            mainLogger.info(page.getRelPath() + " relevance " + String.format("%.2f", r));
        }

        for (Map.Entry<Page, Float> abs : pagesForRelevance.entrySet()) {
            pagesForRelevance.put(abs.getKey(), abs.getValue() / maxRel);
        }
        pagesForRelevance.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .forEachOrdered(x -> sortedMap.put(x.getKey(), x.getValue()));
        mainLogger.info("REL MAX " + maxRel);
        mainLogger.warn("page store size " + sortedMap.size());
        sortedMap.forEach((key, value) -> {
            mainLogger.warn(key.getRelPath() + "\t ==> " + value);
        });


        return sortedMap;
    }

    private List<Lemma> getLemmasFromRequest(String searchLine) {
        List<Lemma> findLemmas = new ArrayList<>();
        try {
            LemmaCounter counter = new LemmaCounter(searchLine);
            Map<String, Integer> lemmas = counter.countLemmas();
            lemmas.forEach((key, value) -> {
                Optional<Lemma> findLemma = lemmaService.findLemma(key);
                findLemma.ifPresentOrElse(findLemmas::add, ArrayList::new);
            });
            deleteCommonLemmas(findLemmas);
        } catch (IOException e) {
            e.printStackTrace();
        }
        sortLemmasByFrequency(findLemmas);
        return findLemmas;
    }

    private Set<Lemma> deleteCommonLemmas(List<Lemma> lemmas) {
        Set<Lemma> modifyLemmaList = new TreeSet<>(lemmas);
        final double percent = 1;
        double frequency = pageService.getAllPages().size() * percent;
        lemmas.removeIf(l -> l.getFrequency() > frequency);
        return modifyLemmaList;
    }

    private List<Lemma> sortLemmasByFrequency(List<Lemma> lemmaSet) {
        return lemmaSet.stream().sorted(Comparator.comparing(Lemma::getFrequency)).collect(Collectors.toCollection(ArrayList::new));
    }


    private List<Page> findIndexedPage(Lemma lemma) {
        List<SearchIndex> findPages = new ArrayList<>();
        if (lemma == null) {
            mainLogger.info("empty lemma indexed");
        } else {
            findPages = new ArrayList<>(searchIndexService.findIndexByLemma(lemma));
        }
        return findPages.stream().map(SearchIndex::getPage).collect(Collectors.toList());
    }

    private Lemma getMinLemma(List<Lemma> lemmaList) {
        if (lemmaList.size() == 1) {
            return lemmaList.get(0);
        } else
            return lemmaList.stream().min(Comparator.comparing(Lemma::getFrequency)).orElse(null);
    }
}
