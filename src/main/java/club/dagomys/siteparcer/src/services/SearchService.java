package club.dagomys.siteparcer.src.services;

import club.dagomys.siteparcer.src.dto.request.SearchRequest;
import club.dagomys.siteparcer.src.dto.response.SearchData;
import club.dagomys.siteparcer.src.dto.response.SearchResponse;
import club.dagomys.siteparcer.src.entity.Lemma;
import club.dagomys.siteparcer.src.entity.Page;
import club.dagomys.siteparcer.src.entity.SearchIndex;
import club.dagomys.siteparcer.src.entity.Site;
import club.dagomys.siteparcer.src.exception.LemmaNotFoundException;
import club.dagomys.siteparcer.src.exception.SearchEngineException;
import club.dagomys.siteparcer.src.lemmatisator.LemmaCounter;
import club.dagomys.siteparcer.src.repositories.LemmaRepository;
import club.dagomys.siteparcer.src.repositories.PageRepository;
import club.dagomys.siteparcer.src.repositories.SearchIndexRepository;
import club.dagomys.siteparcer.src.repositories.SiteRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class SearchService {
    private final Logger mainLogger = LogManager.getLogger(SearchService.class);
    private Site site;

    @Autowired
    private LemmaRepository lemmaRepository;

    @Autowired
    private PageRepository pageRepository;

    @Autowired
    private SiteRepository siteRepository;

    @Autowired
    private SearchIndexRepository searchIndexRepository;

    private LemmaCounter counter;


    public SearchResponse search(SearchRequest searchLine, String site, int offset, int limit, Errors errors) throws SearchEngineException {
        SearchResponse response = new SearchResponse();
        List<Page> findPages = new ArrayList<>();
        List<Lemma> findLemmas = new ArrayList<>();
        Optional<Site> findSite = siteRepository.findByUrl(site);
        try {
            if (!errors.hasErrors()) {
                List<Lemma> requestLemmas = getLemmasFromRequest(searchLine);
                Map<Site, List<Lemma>> siteLemmaMap = getSiteLemmaMap(requestLemmas);
                if (findSite.isPresent()) {
                    this.site = findSite.get();
                    findLemmas.addAll(requestLemmas.stream().filter(lemma -> lemma.getSite().equals(this.site)).toList());
                    findPages.addAll(getFindPages(findLemmas));
                } else {
                    siteLemmaMap.forEach((s, l) -> {
                        findPages.addAll(getFindPages(l));
                        findLemmas.addAll(l);
                    });
                }
                List<SearchData> searchDataList = getRelevance(findPages, findLemmas);

                mainLogger.info("Поисковый запрос \t" + searchLine);
                response.setResult(true);
                response.setCount(searchDataList.size());
                response.setSearchData(getPageFromList(searchDataList, offset, limit).toList());
                mainLogger.info("RESPONSE " + response);
            } else throw new SearchEngineException(Objects.requireNonNull(errors.getFieldError()).getDefaultMessage());
        } catch (SearchEngineException ex) {
            response.setResult(false);
            response.setError(ex.getMessage());
            mainLogger.error(ex.getMessage());
        }
        return response;
    }

    public org.springframework.data.domain.Page<SearchData> getPageFromList(List<SearchData> searchData, int offset, int pageSize) throws SearchEngineException {
        if (offset < searchData.size()) {
            int end = Math.min((offset + pageSize), searchData.size());
            return new PageImpl<>(searchData.subList(offset, end));
        } else throw new SearchEngineException("Неверные параметры пагинации");
    }

    private List<Page> getFindPages(List<Lemma> findSiteLemmas) {
        Lemma minFreqLemma = getMinLemma(findSiteLemmas);
        List<Page> findPages = findIndexedPage(minFreqLemma);
        List<SearchIndex> indexingList = searchIndexRepository.findByLemmaOrderByRankDesc(minFreqLemma);
        List<Page> pages = new ArrayList<>();
        indexingList.forEach(indexing -> pages.add(indexing.getPage()));

        for (Lemma lemma : findSiteLemmas) {
            if (!pages.isEmpty() && lemma != minFreqLemma) {
                List<SearchIndex> secondIndexSearch = searchIndexRepository.findByLemmaOrderByRankDesc(lemma);
                List<Page> pageList = new ArrayList<>();
                secondIndexSearch.forEach(indexing -> pageList.add(indexing.getPage()));
                findPages.retainAll(pageList);
            }
        }
        return findPages;
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
        } else {
            string.append(page.getSite().getUrl()).append(page.getRelPath().replaceFirst("/", "")).append("\">");
        }
        string.append(getTitle(page)).append("</a>");
        return string.toString();

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
            Optional<SearchIndex> searchIndex = searchIndexRepository.findByPageAndLemma(page, lemma);
            if (searchIndex.isPresent()) {
                r = r + searchIndex.get().getRank();
            }
        }
        return r;
    }

    private List<SearchData> getRelevance(List<Page> pageList, List<Lemma> lemmaList) throws SearchEngineException {
        float maxRel = 0f;
        Map<Page, Float> pageAbsRelevance = new HashMap<>();
        List<SearchData> pageRelRelevance = new ArrayList<>();
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
        if (pageRelRelevance.isEmpty()){
            throw new SearchEngineException("По вашему поиску ничего не найдено");
        }
        Collections.sort(pageRelRelevance);
        return pageRelRelevance;
    }


    private List<Lemma> getLemmasFromRequest(SearchRequest searchLine) {
        List<Lemma> lemmaList = new ArrayList<>();
        try {
            counter = new LemmaCounter(searchLine.getSearchLine());
            Map<String, Integer> lemmas = counter.countLemmas();
            lemmas.forEach((key, value) -> {
                try {
                    Optional<List<Lemma>> findLemmas = lemmaRepository.findAllByLemma(key);
                    findLemmas.ifPresent(lemmaList::addAll);
                } catch (LemmaNotFoundException e) {
                    mainLogger.warn(e.getMessage());
                }
            });
            deleteCommonLemmas(lemmaList);
        } catch (IOException e) {
            mainLogger.error(e.getMessage());
        }
        mainLogger.info("FIND LEMMAS \t" + lemmaList);
        return lemmaList;
    }

    private Set<Lemma> deleteCommonLemmas(List<Lemma> lemmas) {
        Set<Lemma> modifyLemmaList = new TreeSet<>(lemmas);
        final double percent = 1;
        double frequency = pageRepository.findAll().size() * percent;
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
            findPages = new ArrayList<>(searchIndexRepository.findByLemmaOrderByRankDesc(lemma));
        }
        return findPages.stream().map(SearchIndex::getPage).collect(Collectors.toList());
    }

    private Lemma getMinLemma(List<Lemma> lemmaList) {
        return lemmaList.stream().min(Comparator.comparing(Lemma::getFrequency)).orElse(null);
    }

    private Map<Site, List<Lemma>> getSiteLemmaMap(List<Lemma> lemmaList) {
        Map<Site, List<Lemma>> lemmaMap = new TreeMap<>();
        for (Lemma l : lemmaList) {
            Site site = l.getSite();
            if (lemmaMap.containsKey(site)) {
                lemmaMap.get(site).add(l);
            } else {
                List<Lemma> lemmas = new ArrayList<>();
                lemmas.add(l);
                lemmaMap.put(l.getSite(), lemmas);
            }
        }
        return lemmaMap;
    }

    private boolean isAbsURL(Page page) {
        Pattern urlPattern = Pattern.compile("(https?://(?:www\\.|(?!www))[a-zA-Z0-9][a-zA-Z0-9-]+[a-zA-Z0-9]\\.\\S{2,}|www\\.[a-zA-Z0-9][a-zA-Z0-9-]+[a-zA-Z0-9]\\.\\S{2,}|https?://(?:www\\.|(?!www))[a-zA-Z0-9]+\\.\\S{2,}|www\\.[a-zA-Z0-9]+\\.\\S{2,})");
        return
                urlPattern.matcher(page.getRelPath()).find();
    }
}
