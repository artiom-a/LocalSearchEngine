package club.dagomys.siteparcer.src.services;

import club.dagomys.lemmatisator.scr.LemmaCounter;
import club.dagomys.siteparcer.src.entity.Lemma;
import club.dagomys.siteparcer.src.entity.Page;
import club.dagomys.siteparcer.src.entity.SearchIndex;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.CriteriaBuilder;
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
                mainLogger.info(lemma.getLemma() + " " + findPages);
                List<SearchIndex> secondIndexSearch = searchIndexService.findIndexByLemma(lemma);
                List<Page> tempList = new ArrayList<>();
                secondIndexSearch.forEach(indexing -> tempList.add(indexing.getPage()));
                mainLogger.info(lemma.getLemma() + " temp - " + tempList);
                findPages.retainAll(tempList);
                mainLogger.info(lemma.getLemma() + " after - " + findPages);
            }
        }
        pageIndexes.forEach(System.out::println);
//        findPages.forEach(index -> {
//            searchIndexService.findIndexByPage(index.getPage()).forEach(System.out::println);
//            findLemmas.forEach(lemma -> {
//                mainLogger.info(lemma);
//                if ((searchIndexService.findIndexByPage(index.getPage()).contains(lemma))) {
//                    list.add(index);
//                }
//            });
//        });
//        list.forEach(System.out::println);
//        findPages.forEach(System.out::println);
        return findPages;
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

/*

    public Map<Page, Double> searching(String searchLine) {
        HashMap<Page, Double> pageRelevance = new HashMap<>();
        List<Lemma> findLemmas = new ArrayList<>(getLemmasFromRequest(searchLine));
        Lemma minFreqLemma = getMinLemma(findLemmas);
        List<Page> pageIndexes = new ArrayList<>();
        if (minFreqLemma != null) {
            List<SearchIndex> indexingList = searchIndexService.findIndexByLemma(minFreqLemma);
            indexingList.forEach(indexing -> pageIndexes.add(indexing.getPage()));
            for (Lemma lemma : findLemmas) {
                if (!pageIndexes.isEmpty() && lemma != minFreqLemma) {
                    List<SearchIndex> indexingList2 = searchIndexService.findIndexByLemma(lemma);
                    List<Page> tempList = new ArrayList<>();
                    indexingList2.forEach(index -> tempList.add(index.getPage()));
                    pageIndexes.retainAll(tempList);
                }
            }
            Map<Page, Double> pageAbsRelevance = new HashMap<>();

            double maxRel = 0.0;
            for (Page p : pageIndexes) {
                Page opPage;
                opPage = pageService.getPageById(p.getId());
                if (opPage != null) {
                    double r = getAbsRelevance(opPage, findLemmas);
                    pageAbsRelevance.put(opPage, r);
                    if (r > maxRel)
                        maxRel = r;
                }
            }
            for (Map.Entry<Page, Double> abs : pageAbsRelevance.entrySet()) {
                pageRelevance.put(abs.getKey(), abs.getValue() / maxRel);
            }
        }
        mainLogger.info(pageRelevance);
        return pageRelevance;
    }

    private double getAbsRelevance(Page page, List<Lemma> lemmas){
        double r = 0.0;
        for (Lemma lemma : lemmas) {
            SearchIndex indexing = searchIndexService.findIndexByPageAndLemma(page, lemma);
            r = r + indexing.getRank();
        }
        return r;
    }*/
