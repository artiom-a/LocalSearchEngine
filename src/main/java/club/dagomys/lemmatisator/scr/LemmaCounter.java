package club.dagomys.lemmatisator.scr;

import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;

import javax.persistence.criteria.CriteriaBuilder;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class LemmaCounter {
    private final LuceneMorphology luceneMorphology;
    private final String text;
    private Map<String, Integer> wordsMap;

    public LemmaCounter(String text) throws IOException {
        this.text = text;
        luceneMorphology = new RussianLuceneMorphology();
        wordsMap = new TreeMap<>();
    }

    public Map<String, Integer> countLemmas() {
        String[] replacedtext = text.replaceAll("—", "")
                .replaceAll("\\p{Punct}|[0-9]", "").split("\\s+");

        List<String> morphList = new ArrayList<>();
        for (String word : replacedtext) {
            morphList = luceneMorphology.getNormalForms(word.toLowerCase(Locale.ROOT));
            for ( String morph : morphList ) {
                Integer oldCount = wordsMap.get(morph);
                if ( oldCount == null ) {
                    oldCount = 0;
                }
                wordsMap.put(morph, oldCount + 1);
            }

        }
        return wordsMap;
    }
}
