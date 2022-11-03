package club.dagomys.siteparcer.src.services;

import club.dagomys.lemmatisator.scr.LemmaCounter;
import club.dagomys.siteparcer.src.entity.Field;
import club.dagomys.siteparcer.src.entity.Lemma;
import club.dagomys.siteparcer.src.entity.Page;
import club.dagomys.siteparcer.src.entity.SearchIndex;
import club.dagomys.siteparcer.src.entity.request.SearchRequest;
import club.dagomys.siteparcer.src.entity.request.SearchResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.util.StringUtils;

import javax.naming.directory.SearchResult;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class SearchService {
    private final Logger mainLogger = LogManager.getLogger(SearchService.class);
    private final Pattern wordPatterRegexp = Pattern.compile("[A-zА-яё][A-zА-яё'^]*");

    @Autowired
    private LemmaService lemmaService;

    @Autowired
    private PageService pageService;

    @Autowired
    private SearchIndexService searchIndexService;

    private LemmaCounter counter;

    public List<SearchResponse> search(SearchRequest searchLine) {
        mainLogger.info("Поисковый запрос \t" + searchLine);
        List<Lemma> findLemmas = new ArrayList<>(getLemmasFromRequest(searchLine.getSearchLine()));
        Lemma minFreqLemma = getMinLemma(findLemmas);
        List<Page> findPages = findIndexedPage(minFreqLemma);
        List<SearchIndex> indexingList = searchIndexService.findIndexByLemma(minFreqLemma);
        List<Page> pageIndexes = new ArrayList<>();
        List<SearchResponse> searchResponses = new ArrayList<>();
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
        pagesForRelevance.forEach((page, relevance) -> {
            SearchResponse response = getResponse(page, relevance, findLemmas);
            searchResponses.add(response);
            mainLogger.info("RESPONSE " + response);
        });

        return searchResponses;
    }

    private SearchResponse getResponse(Page page, float relevance, List<Lemma> requestLemmas) {
        return new SearchResponse(getAbsLink("https://svetlovka.ru", page), getTitle(page), getSnippet(page, requestLemmas), relevance);
    }

    private String getTitle(Page page) {
        Document html = Jsoup.parse(page.getContent());
        String title = html.title();
        if (title.isEmpty()) {
            return "";
        } else
            return title;
    }

    private String getAbsLink(String site, Page page) {
        StringBuilder string = new StringBuilder();
        string.append("<a href=\"");
        if (urlChecker(page)) {
            string.append(page.getRelPath()).append("\">");
            string.append(getTitle(page)).append("</a>");
            return string.toString();
        } else {
            string.append(site).append(page.getRelPath()).append("\">");
            string.append(getTitle(page)).append("</a>");
            return string.toString();
        }

    }

    private List<TreeSet<Integer>> getSearchingIndexes(String string, List<Integer> indexes) {
        List<TreeSet<Integer>> list = new ArrayList<>();
        TreeSet<Integer> snippetSet = new TreeSet<>();
        ListIterator<Integer> iterator = indexes.listIterator();
        while (iterator.hasNext()) {
            Integer index = iterator.next();
            int nextIndex = iterator.nextIndex();
            String s = string.substring(index);
            int end = s.indexOf(' ');
            if (nextIndex <= indexes.size() - 1 && (indexes.get(nextIndex) - index) < end + 4) {
                snippetSet.add(index);
                snippetSet.add(indexes.get(nextIndex));
            } else {
                if (!snippetSet.isEmpty()) {
                    list.add(snippetSet);
                    snippetSet = new TreeSet<>();
                }
                snippetSet.add(index);
                list.add(snippetSet);
                snippetSet = new TreeSet<>();
            }
        }
        list.sort((Comparator<Set<Integer>>) (o1, o2) -> o2.size() - o1.size());
        ArrayList<TreeSet<Integer>> searchingIndexes = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            if (list.size() > i) {
                searchingIndexes.add(list.get(i));
            }
        }
        return searchingIndexes;
    }

    private String getSnippet(Page page, List<Lemma> requestLemmas) {
        StringBuilder string = new StringBuilder();
        Document document = Jsoup.parse(page.getContent());
        string.append(document.text());
        List<Integer> searchIndexes = new ArrayList<>(counter.findLemmaIndexInText(page, requestLemmas));
        List<TreeSet<Integer>> indexesList = getSearchingIndexes(string.toString(), searchIndexes);
        StringBuilder snippetBuilder = new StringBuilder();
        for (TreeSet<Integer> set : indexesList) {
            int from = set.first();
            int to = set.last();
            Pattern pattern = Pattern.compile("\\p{Punct}|\\s");
            Matcher matcher = pattern.matcher(string.substring(to));
            int offset = 0;
            if (matcher.find()) {
                offset = matcher.end();
            }
            snippetBuilder.append("<b>")
                    .append(string, from, to + offset)
                    .append("</b>");
            if (!((string.length() - to) < 10)) {
                snippetBuilder.append(string, to + offset, string.indexOf(" ", to + offset + 10));
            }
            snippetBuilder.append("... ");
        }
        return snippetBuilder.toString();
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

    private boolean urlChecker(Page page) {
        Pattern urlPattern = Pattern.compile("(https?:\\/\\/(?:www\\.|(?!www))[a-zA-Z0-9][a-zA-Z0-9-]+[a-zA-Z0-9]\\.[^\\s]{2,}|www\\.[a-zA-Z0-9][a-zA-Z0-9-]+[a-zA-Z0-9]\\.[^\\s]{2,}|https?:\\/\\/(?:www\\.|(?!www))[a-zA-Z0-9]+\\.[^\\s]{2,}|www\\.[a-zA-Z0-9]+\\.[^\\s]{2,})");
        return
                urlPattern.matcher(page.getRelPath()).find();
    }
}
