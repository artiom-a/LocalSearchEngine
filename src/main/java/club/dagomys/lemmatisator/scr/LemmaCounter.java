package club.dagomys.lemmatisator.scr;

import club.dagomys.siteparcer.src.entity.Lemma;
import club.dagomys.siteparcer.src.entity.Page;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.util.JsonUtils;
import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.english.EnglishLuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

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
    private Matcher wordMatch;
    private List<String> replacedText;
    private String text;

    public LemmaCounter(String text) throws IOException {
        this.text = text;
        wordMatch = wordPatterRegexp.matcher(text.toLowerCase(Locale.ROOT).replaceAll("[—]|\\p{Punct}|\\s]", " "));
        replacedText = wordMatch.results()
                .map(MatchResult::group).toList();
        wordsMap = new TreeMap<>();
    }

    public LemmaCounter() throws IOException {
    }

    public Map<String, Integer> countLemmas() {
        List<String> morphList = replacedText.stream()
                .filter(word -> word.matches(english.pattern()) || word.matches(russian.pattern())).filter(morph -> !isAuxiliaryPartsOfSpeech(morph)).toList();
        wordsMap = morphList.stream()
                .map(word -> Objects.equals(getLanguage(word), "RU") ? russianMorphology.getNormalForms(word) : englishMorphology.getNormalForms(word))
                .collect(Collectors.toMap(l -> l.get(0), v -> 1, Integer::sum));
        wordsMap.keySet().removeIf(l -> l.matches("\\b([A-z]{1,2})\\b")); //remove short 1 till 2 chars symbols
        return wordsMap;
    }

    public ArrayList<Integer> findLemmaIndexInText(Page page, Lemma lemma) {
        ArrayList<Integer> listOfIndexes = new ArrayList<>();
        Document document = Jsoup.parse(page.getContent());

        List<String> pageWordList = wordPatterRegexp
                .matcher(document.text().toLowerCase(Locale.ROOT).replaceAll("[—]|\\p{Punct}|\\s]", " "))
                .results()
                .map(MatchResult::group).toList();
        System.out.println(pageWordList);
        mainLogger.info("list[]\t" + pageWordList);
        int index = 0;
        List<String> lemmas = pageWordList.stream()
                .map(word -> Objects.equals(getLanguage(word), "RU") ? russianMorphology.getNormalForms(word) : englishMorphology.getNormalForms(word))
                .flatMap(Collection::stream).toList();

            for (String s2 : lemmas) {
                if (s2.equals(lemma.getLemma())) {
                    listOfIndexes.add(index);
                }
            }
        System.out.println(lemmas);
        return listOfIndexes;
    }

    public Set<String> getLemmaSet() {
        lemmaSet = new TreeSet<>();
        Set<String> morphList = replacedText.stream()
                .filter(word -> word.matches(english.pattern()) || word.matches(russian.pattern())).filter(morph -> !isAuxiliaryPartsOfSpeech(morph))
                .collect(Collectors.toSet());

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
                        morph.contains("КР_ПРИЛ") |
                        morph.contains("ЧАСТ");
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
