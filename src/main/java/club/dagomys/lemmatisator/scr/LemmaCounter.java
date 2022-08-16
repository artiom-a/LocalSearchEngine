package club.dagomys.lemmatisator.scr;

import club.dagomys.siteparcer.src.entity.Lemma;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.util.JsonUtils;
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
    private final Logger mainLogger = LogManager.getLogger(LemmaCounter.class);
    private Map<String, Integer> wordsMap;
    private Set<String> lemmaSet;
    private final Pattern wordPatterRegexp = Pattern.compile("[A-zА-яё][A-zА-яё'^]*");
    private final Pattern english = Pattern.compile("([A-z]+)");
    private final Pattern russian = Pattern.compile("([А-яё]+)");

    public LemmaCounter() throws IOException {
        wordsMap = new TreeMap<>();
    }

    public Map<String, Integer> countLemmas(String text) {
        Matcher wordMatch = wordPatterRegexp.matcher(text.toLowerCase(Locale.ROOT).replaceAll("[_*|\\/\\\\*|\\[\\]|`]", " "));
        List<String> replacedText = wordMatch.results()
                .map(MatchResult::group)
                .collect(Collectors.toList());
        mainLogger.info(replacedText);

        List<String> morphList = replacedText.stream()
                .filter(word -> word.matches(english.pattern()) || word.matches(russian.pattern())).filter(morph -> !isAuxiliaryPartsOfSpeech(morph))
                .collect(Collectors.toList());
        mainLogger.warn("\t\t" + morphList);

        wordsMap = morphList.stream().map(word -> Objects.equals(getLanguage(word), "RU") ? russianMorphology.getNormalForms(word) : englishMorphology.getNormalForms(word))
                .collect(Collectors.toMap(l -> l.get(0), v -> 1, Integer::sum));
        wordsMap.keySet().removeIf(l -> l.matches("\\b([A-z]{1,2})\\b")); //remove short 1 till 2 chars symbols
        return wordsMap;
    }


    public Set<String> getLemmaSet(String text) {
        Matcher wordMatch = wordPatterRegexp.matcher(text.toLowerCase(Locale.ROOT).replaceAll("[_*|\\/\\\\*|\\[\\]|`]", " "));
        lemmaSet = new TreeSet<>();
        /**
         * Breaking the whole text into words
         */
        List<String> replacedText = wordMatch.results()
                .map(MatchResult::group)
                .collect(Collectors.toList());
        /**
         * Form a list of English and Russian morphs. Cleaning from service parts of speech
         */
        Set<String> morphList = replacedText.stream()
                .filter(word -> word.matches(english.pattern()) || word.matches(russian.pattern())).filter(morph -> !isAuxiliaryPartsOfSpeech(morph))
                .collect(Collectors.toSet());
        /**
         Get set of normal lemmas
         */
        lemmaSet = morphList.stream()
                .map(word -> Objects.equals(getLanguage(word), "RU") ? russianMorphology.getNormalForms(word) : englishMorphology.getNormalForms(word))
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
        return lemmaSet;

    }

    public Map<String, Integer> getWordsMap() {
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
