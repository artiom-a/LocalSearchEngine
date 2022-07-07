package club.dagomys.lemmatisator.scr;

import antlr.TokenStream;
import io.github.kju2.languagedetector.LanguageDetector;
import io.github.kju2.languagedetector.language.Language;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class LemmaCounter {
    private final LuceneMorphology russianMorphology = new RussianLuceneMorphology();
    private final LuceneMorphology englishMorphology = new EnglishLuceneMorphology();
    private final String text;
    private Map<String, Long> wordsMap;
    private final LanguageDetector detector = new LanguageDetector();

    public LemmaCounter(String text) throws IOException {
        this.text = text;
        countLemmas();
    }

    private Map<String, Long> countLemmas() {

        Language checkLanguage = detector.detectPrimaryLanguageOf(text);
        if (checkLanguage.code.equals("ru") || checkLanguage.code.equals("en")) {
            wordsMap = new TreeMap<>();
            String[] replacedtext = text.toLowerCase(Locale.ROOT)
                    .replaceAll("—", "")
                    .replaceAll("-", " ")
                    .replaceAll("\\p{Punct}|[0-9]", "")
                    .split("\\s+");

            List<String> morphList = Arrays.stream(replacedtext)
                    .filter(morph -> !isAuxiliaryPartsOfSpeech(morph))
                    .collect(Collectors.toList());
            wordsMap = morphList
                    .stream()
                    .map(word -> Objects.equals(getLanguage(word), "RU") ? russianMorphology.getNormalForms(word) : englishMorphology.getNormalForms(word))
                    .collect(Collectors.groupingBy(normalWord -> normalWord.get(0), Collectors.counting()));
            return wordsMap;
        } else {
            return null;
        }
    }


    public Map<String, Long> getWordsMap() {
        return wordsMap;
    }

    private boolean isAuxiliaryPartsOfSpeech(String word) {
        if (Objects.equals(getLanguage(word), "RU")) {
            for (String morph : russianMorphology.getMorphInfo(word)) {
                return morph.contains("СОЮЗ") |
                        morph.contains("МЕЖД") |
                        morph.contains("МС") |
                        morph.contains("ПРЕДЛ") |
                        morph.contains("КР_ПРИЛ");
            }
        } else if (Objects.equals(getLanguage(word), "EN")) {
            for (String morph : englishMorphology.getMorphInfo(word)) {
                return morph.contains("CONJ") |
                        morph.contains("INT") |
                        morph.contains("PRON") |
                        morph.contains("ARTICLE") |
                        morph.contains("PREP") |
                        morph.contains("PART");
            }
        } else
            System.out.println("Язык не распознан");
        return false;
    }


    private String getLanguage(String word) {
        for (int i = 0; i < word.length(); i++) {
            if (Character.UnicodeBlock.of(word.charAt(i)).equals(Character.UnicodeBlock.CYRILLIC)) {
                return "RU";
            } else if (Character.UnicodeBlock.of(word.charAt(i)).equals(Character.UnicodeBlock.BASIC_LATIN)) {
                return "EN";
            } else {
                return "Can't check language";
            }
        }
        return null;
    }
}
