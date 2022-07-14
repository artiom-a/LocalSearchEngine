package club.dagomys.lemmatisator.scr;

import antlr.TokenStream;
import club.dagomys.siteparcer.src.entity.MainLog4jLogger;
import io.github.kju2.languagedetector.LanguageDetector;
import io.github.kju2.languagedetector.language.Language;
import org.apache.logging.log4j.Logger;
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
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class LemmaCounter {
    private final LuceneMorphology russianMorphology = new RussianLuceneMorphology();
    private final LuceneMorphology englishMorphology = new EnglishLuceneMorphology();
    Logger mainLogger = MainLog4jLogger.getIstance();
    private final String text;
    private Map<String, Long> wordsMap;
    private final LanguageDetector detector = new LanguageDetector();
    private final Pattern wordPatterRegexp =  Pattern.compile("[A-Za-zА-Яа-яё][A-Za-zА-Яа-яё'^]*");

    public LemmaCounter(String text) throws IOException {
        this.text = text;
        countLemmas();
    }

    private Map<String, Long> countLemmas() {
        Language checkLanguage = detector.detectPrimaryLanguageOf(text);
        mainLogger.info(checkLanguage);
        if (checkLanguage.code.equals("ru") || checkLanguage.code.equals("en")) {
            wordsMap = new TreeMap<>();
            //replaceAll("([—|-|–|©|·|№|°|←|→|?|’|`]|\\p{Punct}|[0-9]|[«»]|[()])", " ");
//            System.out.println((int) Arrays.stream(text.toLowerCase(Locale.ROOT)
//                    .split("\\s+")).count()); Посчитать выборку
            Matcher wordMatch = wordPatterRegexp.matcher(text.toLowerCase(Locale.ROOT));
            List<String> replacedtext = wordMatch.results().map(MatchResult::group).collect(Collectors.toList());

            List<String> morphList = replacedtext.stream()
                    .filter(morph -> !isAuxiliaryPartsOfSpeech(morph))
                    .collect(Collectors.toList());
            wordsMap = morphList
                    .stream()
                    .map(word -> Objects.equals(getLanguage(word), "RU") ? russianMorphology.getNormalForms(word) : englishMorphology.getNormalForms(word))
                    .collect(Collectors.groupingBy(normalWord -> normalWord.get(0), Collectors.counting()));
            return wordsMap;
        } else {
            return new TreeMap<>();
        }
    }


    public Map<String, Long> getWordsMap() {
        return wordsMap;
    }

    private boolean isAuxiliaryPartsOfSpeech(String word) {
        if(word.matches(""))
        if (Objects.equals(getLanguage(word), "RU")) {
            for (String morph : russianMorphology.getMorphInfo(word)) {
                mainLogger.info("RU \t"+morph);
                return morph.contains("СОЮЗ") |
                        morph.contains("МЕЖД") |
                        morph.contains("МС") |
                        morph.contains("ПРЕДЛ") |
                        morph.contains("КР_ПРИЛ");
            }
        } else if (Objects.equals(getLanguage(word), "EN")) {
            for (String morph : englishMorphology.getMorphInfo(word)) {
                mainLogger.info("EN \t"+morph);
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

//    private String languageCheck (String word) {
//        Language checkLanguage = detector.detectPrimaryLanguageOf(word);
//        for (int i = 0; i < word.length(); i++) {
//            if (checkLanguage.code.equals("ru")) {
//                return "RU";
//            } else if (checkLanguage.code.equals("en")) {
//                return "EN";
//            } else {
//                return "Can't check language";
//            }
//        }
//        return null;
//    }
}
