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
    private final Map<String, Integer> wordsMap;

    public LemmaCounter(String text) throws IOException {
        this.text = text;
        russianMorphology = new RussianLuceneMorphology();
        wordsMap = new TreeMap<>();
        countLemmas();
    }

    private Map<String, Integer> countLemmas() {
        String[] replacedtext = text.toLowerCase(Locale.ROOT).replaceAll("—", "")
                .replaceAll("\\p{Punct}|[0-9]", "").split("\\s+");

        List<String> morphList = Arrays.stream(replacedtext).map(russianMorphology::getMorphInfo).filter(morph -> !isAuxiliaryPartsOfSpeech(morph.get(0))).flatMap(List::stream).collect(Collectors.toList());
        Map<String, Long> normList = Arrays.stream(replacedtext).map(russianMorphology::getNormalForms).collect(Collectors.groupingBy(normalWord->normalWord.get(0), Collectors.counting()));




        morphList.forEach(System.out::println);
        normList.entrySet().forEach(System.out::println);
//        normList.forEach(System.out::println);
        List<String> necessaryWords = new ArrayList<>();
//        for (String morph : morphList) {
//            if (!isAuxiliaryPartsOfSpeech(morph)) {
//                necessaryWords.add(morph);
//            }
//
//        }

//        for (String s : replacedtext){
//            System.out.println(russianMorphology.getMorphInfo(s).size());
//            russianMorphology.getMorphInfo(s).forEach(System.out::println);
//        }
        List<String> normalFormList;
        for (String morph : necessaryWords) {
            normalFormList = russianMorphology.getNormalForms(morph);
            for (String word : normalFormList) {
                wordsMap.merge(word, 1, Integer::sum);
            }

        }
//        wordsMap.keySet().stream().map(russianMorphology::getMorphInfo).collect(Collectors.toSet()).forEach(System.out::println);

//        wordsMap.keySet().forEach(russianMorphology::getNormalForms);
//        wordsMap.entrySet().forEach(System.out::println);

        return wordsMap;
    }


    public Map<String, Integer> getWordsMap() {
        return wordsMap;
    }

    private boolean isAuxiliaryPartsOfSpeech(String word) {
        return word.contains("СОЮЗ") |
                word.contains("МЕЖД") |
                word.contains("МС") |
//                word.contains("МС-П") |
                word.contains("ПРЕДЛ") |
                word.contains("КР_ПРИЛ");
    }
}
