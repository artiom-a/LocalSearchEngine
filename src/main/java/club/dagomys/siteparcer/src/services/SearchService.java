package club.dagomys.siteparcer.src.services;

import club.dagomys.lemmatisator.scr.LemmaCounter;
import club.dagomys.siteparcer.src.entity.Lemma;
import club.dagomys.siteparcer.src.entity.Page;
import club.dagomys.siteparcer.src.entity.SearchIndex;
import club.dagomys.siteparcer.src.entity.request.SearchRequest;
import club.dagomys.siteparcer.src.entity.request.SearchResponse;
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
        getRelRelevance(findPages, findLemmas).forEach((key, value) -> {
            SearchResponse response = getResponse(key, value, searchLine.getSearchLine());
            mainLogger.warn(response);
        });

        return getRelRelevance(findPages, findLemmas).keySet().stream().toList();
    }

    private SearchResponse getResponse(Page page, float relevance, String searchLine) {

        return new SearchResponse(page.getRelPath(), getTitle(page), getSnippet(page, searchLine), relevance);
    }

    private String getTitle(Page page) {
        Document html = Jsoup.parse(page.getContent());
        String title = html.title();
        if (title.isEmpty()) {
            return "";
        } else
            return title;
    }


    private String getSnippet(Page page, String searchLine) {

/*        LemmaCounter analyzer;
        try {
            analyzer = new LemmaCounter(request);
        } catch (Exception e) {
            mainLogger.warn(" \t" + e.getMessage());
        }*/
        List<Lemma> requestLemma = getLemmasFromRequest(searchLine);
        String string = "";
        Document document = Jsoup.parse(page.getContent());
//        Elements titleElements = document.select("title");
//        Elements bodyElements = document.select("body");
//        StringBuilder builder = new StringBuilder();
//        titleElements.forEach(element -> builder.append(element.text()).append(" ").append("\n"));
//        bodyElements.forEach(element -> builder.append(element.text()).append(" "));
        if (!document.text().isEmpty()) {
            string = document.text();
        }

//        List<TreeSet<Integer>> indexesList = getSearchingIndexes(string);
//        StringBuilder builder1 = new StringBuilder();
//        for (TreeSet<Integer> set : indexesList) {
//            int from = set.first();
//            int to = set.last();
//            Pattern pattern = Pattern.compile("\\p{Punct}|\\s");
//            Matcher matcher = pattern.matcher(string.substring(to));
//            int offset = 0;
//            if (matcher.find()) {
//                offset = matcher.end();
//            }
//            builder1.append("<b>")
//                    .append(string, from, to + offset)
//                    .append("</b>");
//            if (!((string.length() - to) < 30)) {
//                builder1.append(string, to + offset, string.indexOf(" ", to + offset + 30))
//                        .append("... ");
//            }
//        }
        return string;
    }

    private float getAbsRelevance(Page page, List<Lemma> lemmas) {
        float r = 0f;
        for (Lemma lemma : lemmas) {
            SearchIndex searchIndex = null;
            mainLogger.warn(counter.findLemmaIndexInText(page, lemma));
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
                .forEachOrdered(pageFloatEntry -> sortedMap.put(pageFloatEntry.getKey(), pageFloatEntry.getValue()));
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
        return findLemmas;
    }

    private Set<Lemma> deleteCommonLemmas(List<Lemma> lemmas) {
        Set<Lemma> modifyLemmaList = new TreeSet<>(lemmas);
        final double percent = 0.99;
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
