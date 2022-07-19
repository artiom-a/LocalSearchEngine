package club.dagomys.siteparcer.src.services;

import club.dagomys.lemmatisator.scr.LemmaCounter;
import club.dagomys.siteparcer.src.entity.FieldSelector;
import club.dagomys.siteparcer.src.entity.Lemma;
import club.dagomys.siteparcer.src.entity.Page;
import club.dagomys.siteparcer.src.entity.SearchIndex;
import club.dagomys.siteparcer.src.repos.SearchIndexRepository;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

@Service
public class SearchIndexService {

    @Autowired
    private SearchIndexRepository searchIndexRepository;

    @Autowired
    private LemmaService lemmaService;

    @Autowired
    private FieldService fieldService;

    public SearchIndex saveIndex(SearchIndex searchIndex) {
        return searchIndexRepository.save(searchIndex);
    }


    public Map<String, Long> startIndexingPage(Page indexingPage, FieldSelector selector) {
        Document doc = Jsoup.parse(indexingPage.getContent());
        Optional<Map<String, Long>> lemmas = Optional.empty();
        try {
            lemmas = Optional.of(new LemmaCounter(doc.getElementsByTag(fieldService.getFieldByName(selector).getName()).text()).getWordsMap());

            lemmas.get().forEach((key, value) -> {
                Lemma lemma = new Lemma(key, value.intValue());
                lemmaService.saveLemma(lemma);
            });

        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        return lemmas.get();
    }
}
