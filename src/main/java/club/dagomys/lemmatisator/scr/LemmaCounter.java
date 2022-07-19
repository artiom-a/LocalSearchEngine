package club.dagomys.lemmatisator.scr;

import club.dagomys.siteparcer.src.entity.MainLog4jLogger;
import io.github.kju2.languagedetector.LanguageDetector;
import io.github.kju2.languagedetector.language.Language;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.english.EnglishLuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;

import java.io.IOException;
import java.util.*;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class LemmaCounter {
    private final LuceneMorphology russianMorphology = new RussianLuceneMorphology();
    private final LuceneMorphology englishMorphology = new EnglishLuceneMorphology();
    private final Logger mainLogger = MainLog4jLogger.getInstance();
    private final String text;
    private Map<String, Long> wordsMap;
    private final LanguageDetector detector = new LanguageDetector();
    private final Pattern wordPatterRegexp = Pattern.compile("[A-Za-zА-Яа-яё][A-Za-zА-Яа-яё'^]*");
    private final Pattern english = Pattern.compile("([A-z]+)");
    private final Pattern russian = Pattern.compile("([А-яё]+)");

    public LemmaCounter(String text) throws IOException {
        this.text = text;
        countLemmas();
    }

    private Map<String, Long> countLemmas() {
        Matcher wordMatch = wordPatterRegexp.matcher(text.toLowerCase(Locale.ROOT));
        wordsMap = new TreeMap<>();
        List<String> replacedtext = wordMatch.results().map(MatchResult::group).collect(Collectors.toList());
        List<String> morphList = replacedtext.stream().filter(word -> word.matches(english.pattern()) || word.matches(russian.pattern())).filter(morph -> !isAuxiliaryPartsOfSpeech(morph)).collect(Collectors.toList());
        wordsMap = morphList.stream().map(word -> Objects.equals(getLanguage(word), "RU") ? russianMorphology.getNormalForms(word) : englishMorphology.getNormalForms(word)).collect(Collectors.groupingBy(normalWord -> normalWord.get(0), Collectors.counting()));
        return wordsMap;
    }


    public Map<String, Long> getWordsMap() {
        return wordsMap;
    }

    private boolean isAuxiliaryPartsOfSpeech(String word) {

        if (Objects.equals(getLanguage(word), "RU")) {
//            mainLogger.info("RU \t" + word);
            for (String morph : russianMorphology.getMorphInfo(word)) {
                return morph.contains("СОЮЗ") |
                        morph.contains("МЕЖД") |
                        morph.contains("МС") |
                        morph.contains("ПРЕДЛ") |
                        morph.contains("КР_ПРИЛ");
            }
        } else if (Objects.equals(getLanguage(word), "EN")) {
//            mainLogger.info("EN \t" + word);
            for (String morph : englishMorphology.getMorphInfo(word)) {
                return morph.contains("CONJ") |
                        morph.contains("INT") |
                        morph.contains("PRON") |
                        morph.contains("ARTICLE") |
                        morph.contains("PREP") |
                        morph.contains("PART");
            }
        } else System.out.println("Язык не распознан");
        return false;
    }


    private String getLanguage(String word) {
        for (int i = 0; i < word.length(); i++) {
            if (Character.UnicodeBlock.of(word.charAt(i)).equals(Character.UnicodeBlock.CYRILLIC) | word.matches(russian.pattern())) {
                return "RU";
            } else if (Character.UnicodeBlock.of(word.charAt(i)).equals(Character.UnicodeBlock.BASIC_LATIN) | word.matches(english.pattern())) {
                return "EN";
            } else {
                return new Exception("Can't check language").getMessage();
            }
        }
        return null;
    }

}
