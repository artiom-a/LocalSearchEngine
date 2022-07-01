package club.dagomys.lemmatisator.scr;

import antlr.TokenStream;
import org.apache.lucene.analysis.morphology.MorphologyFilterFactory;
import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.Morphology;
import org.apache.lucene.morphology.MorphologyImpl;
import org.apache.lucene.morphology.english.EnglishLuceneMorphology;
import org.apache.lucene.morphology.russian.RussianAnalyzer;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class LemmaCounter {
    private final LuceneMorphology russianMorphology;
    private final String text;
    private Map<String, Long> wordsMap;

    public LemmaCounter(String text) throws IOException {
        this.text = text;
        russianMorphology = new RussianLuceneMorphology();
        wordsMap = new TreeMap<>();
        countLemmas();
    }

    private Map<String, Long> countLemmas() {
        String[] replacedtext = text.toLowerCase(Locale.ROOT).replaceAll("—", "")
                .replaceAll("\\p{Punct}|[0-9]", "").split("\\s+");
        List<String> morphList = Arrays.stream(replacedtext).filter(morph -> !isAuxiliaryPartsOfSpeech(morph)).collect(Collectors.toList());
        wordsMap = morphList.stream().map(russianMorphology::getNormalForms).collect(Collectors.groupingBy(normalWord -> normalWord.get(0), Collectors.counting()));
        return wordsMap;
    }


    public Map<String, Long> getWordsMap() {
        return wordsMap;
    }

    private boolean isAuxiliaryPartsOfSpeech(String word) {
        List<String> morphs = russianMorphology.getMorphInfo(word);
        for (String morph : morphs) {
            return morph.contains("СОЮЗ") |
                    morph.contains("МЕЖД") |
                    morph.contains("МС") |
                    morph.contains("ПРЕДЛ") |
                    morph.contains("КР_ПРИЛ");
        }
        return false;
    }
}
