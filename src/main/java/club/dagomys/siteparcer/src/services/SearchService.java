package club.dagomys.siteparcer.src.services;

import club.dagomys.lemmatisator.scr.LemmaCounter;
import club.dagomys.siteparcer.src.entity.Lemma;
import club.dagomys.siteparcer.src.entity.Page;
import club.dagomys.siteparcer.src.entity.SearchIndex;
import club.dagomys.siteparcer.src.entity.request.SearchRequest;
import club.dagomys.siteparcer.src.entity.request.SearchResponse;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class SearchService {
    private final Logger mainLogger = LogManager.getLogger(SearchService.class);

    @Autowired
    private LemmaService lemmaService;

    @Autowired
    private PageService pageService;

    @Autowired
    private SearchIndexService searchIndexService;

    private LemmaCounter counter;

    public List<Page> search(SearchRequest searchLine) {
        mainLogger.debug("Поисковый запрос \t" + searchLine);
        List<Lemma> findLemmas = new ArrayList<>(getLemmasFromRequest(searchLine.getSearchLine()));
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
        Map<Page, Float> pagesForRelevance = getRelRelevance(findPages, findLemmas);
        pagesForRelevance.forEach((key, value) -> {
            SearchResponse response = getResponse(key, value, findLemmas);
            mainLogger.warn("RESPONSE \t" + new SearchResponse(key.getRelPath(), getTitle(key), getSnippet(key, findLemmas), value));
//            mainLogger.debug("RESPONSE " + response);
        });

        return pagesForRelevance.keySet().stream().toList();
    }

    private SearchResponse getResponse(Page page, float relevance, List<Lemma> lemmas) {

        return new SearchResponse(page.getRelPath(), getTitle(page), getSnippet(page, lemmas), relevance);
    }

    private String getTitle(Page page) {
        Document html = Jsoup.parse(page.getContent());
        String title = html.title();
        if (title.isEmpty()) {
            return "";
        } else
            return title;
    }


    private String getSnippet(Page page, List<Lemma> requestLemmas) {
        StringBuilder string = new StringBuilder();
        Document document = Jsoup.parse(page.getContent());
        List<Integer> searchIndexes = new ArrayList<>();
        requestLemmas.forEach(lemma -> {
            searchIndexes.addAll(KMPSearch(document.text(), lemma));
//            searchIndexes.addAll(counter.findLemmaIndexInText(page, lemma));
        });
        searchIndexes.forEach(index -> {
            if (!document.text().isEmpty()) {
                string.append(document.text(), index, index + 30);
                string.append(" ");
            }
        });
        return string.toString();
    }

    private float getAbsRelevance(Page page, List<Lemma> lemmas) {
        float r = 0f;
        for (Lemma lemma : lemmas) {
            SearchIndex searchIndex = null;
            try {
                searchIndex = searchIndexService.findIndexByPageAndLemma(page, lemma);
                r = r + searchIndex.getRank();
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
                .forEachOrdered(pageFloatEntry -> sortedMap.put(pageFloatEntry.getKey(), pageFloatEntry.getValue()));
        mainLogger.info("RELEVANCE MAX \t" + maxRel);
        mainLogger.warn("page store size " + sortedMap.size());
        sortedMap.forEach((key, value) -> {
            mainLogger.warn(key.getRelPath() + "\t ==> " + value);
        });


        return sortedMap;
    }

    private List<Lemma> getLemmasFromRequest(String searchLine) {
        List<Lemma> findLemmas = new ArrayList<>();
        try {
            counter = new LemmaCounter(searchLine);
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
        mainLogger.info("FIND LEMMAS \t" + findLemmas);
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

    private int[] prefixFunction(Lemma lemma) {
        String pattern = lemma.getLemma();
        int[] values = new int[pattern.length()];
        for (int i = 1; i < pattern.length(); i++) {
            int j = 0;
            while (i + j < pattern.length() && pattern.charAt(j) == pattern.charAt(i + j)) {
                values[i + j] = Math.max(values[i + j], j + 1);
                j++;
            }
        }
        return values;
    }

    private ArrayList<Integer> KMPSearch(String text, Lemma lemma) {
        String searchLine = lemma.getLemma();
        ArrayList<Integer> foundIndexes = new ArrayList<>();
        int[] prefixFunction = prefixFunction(lemma);
        int i = 0;
        int j = 0;
        while (i < text.length()) {
            if (searchLine.charAt(j) == text.charAt(i)) {
                j++;
                i++;
            }
            if (j == searchLine.length()) {
                foundIndexes.add(i - j);
                j = prefixFunction[j - 1];
            } else if (i < text.length() && searchLine.charAt(j) != text.charAt(i)) {
                if (j != 0) {
                    j = prefixFunction[j - 1];
                } else {
                    i = i + 1;
                }
            }
        }
        return foundIndexes;
    }
}
