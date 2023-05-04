package club.dagomys.siteparcer.src.services;

import club.dagomys.siteparcer.src.entity.Site;
import club.dagomys.siteparcer.src.entity.response.SearchData;
import club.dagomys.siteparcer.src.lemmatisator.LemmaCounter;
import club.dagomys.siteparcer.src.entity.Lemma;
import club.dagomys.siteparcer.src.entity.Page;
import club.dagomys.siteparcer.src.entity.SearchIndex;
import club.dagomys.siteparcer.src.entity.request.SearchRequest;
import club.dagomys.siteparcer.src.entity.response.SearchResponse;
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
    private final Pattern wordPatterRegexp = Pattern.compile("[A-zА-яё][A-zА-яё'^]*");
    private Site site;
    private Set<Site> siteSet = new TreeSet<>();

    @Autowired
    private LemmaService lemmaService;

    @Autowired
    private PageService pageService;

    @Autowired
    private SiteService siteService;

    @Autowired
    private SearchIndexService searchIndexService;

    private LemmaCounter counter;


    public SearchResponse search(SearchRequest searchLine, String site) {
        SearchResponse response = new SearchResponse();
        Set<SearchData> searchDataSet = new TreeSet<>();
        List<Lemma> findLemmas;
        List<Site> sites = siteService.getAllSites();
        Optional<Site> findSite = siteService.getSite(site);
        if (findSite.isPresent()) {
            this.site = findSite.get();
            findLemmas = getLemmasFromRequest(searchLine, this.site);
            searchDataSet = getSearchData(findLemmas);
        } else {
            for (Site s : sites) {
                searchDataSet.addAll(getSearchData(getLemmasFromRequest(searchLine, s)));
            }
        }
        mainLogger.info("Поисковый запрос \t" + searchLine);
        if (searchDataSet.isEmpty() || searchLine.isEmpty()) {
            response.setResult(false);
            response.setError("По запросу ничего не найдено или задан пустой поисковый запрос");
            response.setSearchData(searchDataSet);
        } else {
            response.setResult(true);
            response.setCount(searchDataSet.size());
        }
        response.setSearchData(searchDataSet);
        mainLogger.info("RESPONSE " + response);
        return response;
    }

    private Set<SearchData> getSearchData(List<Lemma> findSiteLemmas) {
        Lemma minFreqLemma = getMinLemma(findSiteLemmas);
        List<Page> findPages = findIndexedPage(minFreqLemma);
        List<SearchIndex> indexingList = searchIndexService.findIndexByLemma(minFreqLemma);
        List<Page> pages = new ArrayList<>();
        indexingList.forEach(indexing -> pages.add(indexing.getPage()));

        for (Lemma lemma : findSiteLemmas) {
            if (!pages.isEmpty() && lemma != minFreqLemma) {
                List<SearchIndex> secondIndexSearch = searchIndexService.findIndexByLemma(lemma);
                List<Page> pageList = new ArrayList<>();
                secondIndexSearch.forEach(indexing -> pageList.add(indexing.getPage()));
                findPages.retainAll(pageList);
            }
        }
        return getRelRelevance(findPages, findSiteLemmas);
    }

    private String getTitle(Page page) {
        Document html = Jsoup.parse(page.getContent());
        String title = html.title();
        if (title.isEmpty()) {
            return "";
        } else
            return title;
    }

    private String getAbsLink(Page page) {
        StringBuilder string = new StringBuilder();
        string.append("<a href=\"");
        if (isAbsURL(page)) {
            string.append(page.getRelPath()).append("\">");
            string.append(getTitle(page)).append("</a>");
            return string.toString();
        } else {
            string.append(page.getSite().getUrl()).append(page.getRelPath().replaceFirst("/", "")).append("\">");
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

    private Set<SearchData> getRelRelevance(List<Page> pageList, List<Lemma> lemmaList) {
        float maxRel = 0f;
        Map<Page, Float> pageAbsRelevance = new LinkedHashMap<>();
        Set<SearchData> pageRelRelevance = new TreeSet<>();
        for (Page page : pageList) {
            float r = getAbsRelevance(page, lemmaList);
            pageAbsRelevance.put(page, r);
            if (r > maxRel)
                maxRel = r;
            mainLogger.info(page.getRelPath() + " relevance " + String.format("%.2f", r));
        }

        for (Map.Entry<Page, Float> abs : pageAbsRelevance.entrySet()) {
            Page p = abs.getKey();
            float relevance = abs.getValue();
            pageRelRelevance.add(new SearchData(p, getTitle(p), getSnippet(p, lemmaList), relevance / maxRel));
        }
        return pageRelRelevance;
    }


    private List<Lemma> getLemmasFromRequest(SearchRequest searchLine, Site site) {
        List<Lemma> lemmaList = new ArrayList<>();
        try {
            counter = new LemmaCounter(searchLine.getSearchLine());
            Map<String, Integer> lemmas = counter.countLemmas();
            lemmas.forEach((key, value) -> {
                Optional<Lemma> findLemmas = lemmaService.findByLemmaAndSite(key, site);
                findLemmas.ifPresent(lemmaList::add);
            });
            siteSet = lemmaList.stream().map(Lemma::getSite).collect(Collectors.toSet());
            deleteCommonLemmas(lemmaList);
        } catch (IOException e) {
            e.printStackTrace();
        }
//        sortLemmasByFrequency(lemmaList);
        mainLogger.info("FIND LEMMAS \t" + lemmaList);
        return lemmaList;
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
            mainLogger.error("lemma not exist");
        } else {
            findPages = new ArrayList<>(searchIndexService.findIndexByLemma(lemma));
        }
        return findPages.stream().map(SearchIndex::getPage).collect(Collectors.toList());
    }

    private Lemma getMinLemma(List<Lemma> lemmaList) {
        return lemmaList.stream().min(Comparator.comparing(Lemma::getFrequency)).orElse(null);
    }

    private boolean isAbsURL(Page page) {
        Pattern urlPattern = Pattern.compile("(https?:\\/\\/(?:www\\.|(?!www))[a-zA-Z0-9][a-zA-Z0-9-]+[a-zA-Z0-9]\\.[^\\s]{2,}|www\\.[a-zA-Z0-9][a-zA-Z0-9-]+[a-zA-Z0-9]\\.[^\\s]{2,}|https?:\\/\\/(?:www\\.|(?!www))[a-zA-Z0-9]+\\.[^\\s]{2,}|www\\.[a-zA-Z0-9]+\\.[^\\s]{2,})");
        return
                urlPattern.matcher(page.getRelPath()).find();
    }
}
