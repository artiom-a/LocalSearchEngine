package club.dagomys.siteparcer.src.utils.lemmatizator;

import club.dagomys.siteparcer.src.entity.Lemma;
import club.dagomys.siteparcer.src.entity.Page;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.english.EnglishLuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@Slf4j
public class LemmaCounter {
    private final LuceneMorphology russianMorphology = new RussianLuceneMorphology();
    private final LuceneMorphology englishMorphology = new EnglishLuceneMorphology();
    @Getter
    private Map<String, Integer> wordsMap;
    private final Pattern wordPatterRegexp = Pattern.compile("[A-zА-яё][A-zА-яё'^]*");
    private final Pattern english = Pattern.compile("([A-z]+)");
    private final Pattern russian = Pattern.compile("([А-яё]+)");
    private final List<String> replacedText;

    public LemmaCounter(String text) throws IOException {
        Matcher wordMatch = wordPatterRegexp.matcher(text.toLowerCase(Locale.ROOT).replaceAll("[—]|\\p{Punct}|\\s]", " "));
        replacedText = wordMatch.results()
                .map(MatchResult::group).toList();
        wordsMap = new TreeMap<>();
    }

    public Map<String, Integer> countLemmas() {
        List<String> morphList = replacedText.stream()
                .filter(word -> word.matches(english.pattern()) || word.matches(russian.pattern())).filter(this::isAuxiliaryPartsOfSpeech).toList();
        wordsMap = morphList.stream()
                .map(word -> Objects.equals(getLanguage(word), "RU") ? russianMorphology.getNormalForms(word) : englishMorphology.getNormalForms(word))
                .collect(Collectors.toMap(l -> l.get(0).replace('ё', 'е'), v -> 1, Integer::sum));
        wordsMap.keySet().removeIf(l -> l.matches("\\b([A-z]{1,2})\\b") || l.matches("\\b([А-яё]{1,2})\\b"));//remove short 1 till 2 chars symbols
        return wordsMap;
    }

    public ArrayList<Integer> findLemmaIndexInText(Page page, List<Lemma> lemmaList) {
        ArrayList<Integer> listOfIndexes = new ArrayList<>();
        String document = Jsoup.parse(page.getContent()).text();
        String[] pageWordArray = document.split("\\s+");
        int index = 0;
        for (String splitWord : pageWordArray) {
            String word = splitWord.toLowerCase(Locale.ROOT).replaceAll("[—]|\\p{Punct}|\\s", " ");
            if (word.matches(wordPatterRegexp.pattern()) && word.matches(english.pattern()) || word.matches(russian.pattern())) {
                List<String> lemmas = Stream.of(word)
                        .map(w -> Objects.equals(getLanguage(w), "RU") ? russianMorphology.getNormalForms(w) : englishMorphology.getNormalForms(w))
                        .flatMap(Collection::stream).toList();
                for (Lemma requestLemma : lemmaList) {
                    if (lemmas.contains(requestLemma.getLemma())) {
                        listOfIndexes.add(index);
                    }
                }
            }
            index += splitWord.length() + 1;
        }

        log.info("INDEXES\t" + listOfIndexes);
        return listOfIndexes;
    }


    private boolean isAuxiliaryPartsOfSpeech(String word) {

        if (Objects.equals(getLanguage(word), "RU")) {
            for (String morph : russianMorphology.getMorphInfo(word)) {
                return !(morph.contains("СОЮЗ") |
                        morph.contains("МЕЖД") |
                        morph.contains("МС") |
                        morph.contains("ПРЕДЛ") |
                        morph.contains("КР_ПРИЛ") |
                        morph.contains("ЧАСТ"));
            }
        } else if (Objects.equals(getLanguage(word), "EN")) {
            for (String morph : englishMorphology.getMorphInfo(word)) {
                return !(morph.contains("CONJ") |
                        morph.contains("INT") |
                        morph.contains("PRON") |
                        morph.contains("ARTICLE") |
                        morph.contains("PREP") |
                        morph.contains("PART"));
            }
        } else System.out.println("Язык не распознан");
        return true;
    }


    private String getLanguage(String word) {

        if (word.matches(russian.pattern())) {
            return "RU";
        } else if (word.matches(english.pattern())) {
            return "EN";
        } else {
            return new Exception("Can't check language").getMessage();
        }
    }

}
