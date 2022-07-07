package club.dagomys.lemmatisator.scr;

import io.github.kju2.languagedetector.LanguageDetector;
import io.github.kju2.languagedetector.language.Language;
import org.apache.lucene.morphology.Heuristic;
import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.Morphology;
import org.apache.lucene.morphology.MorphologyImpl;
import org.apache.lucene.morphology.analyzer.MorphologyAnalyzer;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;

import javax.persistence.criteria.CriteriaBuilder;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class LimmatisatorMain {
    public static void main(String[] args) {
        try {
            LuceneMorphology luceneMorphology = new RussianLuceneMorphology();
            String russianText = "Повторное появление леопарда в Осетии позволяет предположить, что леопард постоянно обитает в некоторых районах Северного Кавказа.";
            String englishText = "I am sure learning foreign languages is very important nowadays. People start learning a foreign language, because they want to have a better job, a possibility to study abroad or take part in international conferences. People want to have a possibility to get a higher education abroad or even start their career there. The most popular among foreign languages are English, German, Italian, French and Spanish.";
            String french = "Ma ville\n" +
                    "J'habite une belle ville dans le nord de la France. Il y a un quartier très agréable pour aller se balader, lorsqu'il fait beau. Il est possible de faire du vélo dans un parc autour d'une citadelle fortifiée.";


            LemmaCounter counter = new LemmaCounter(russianText);
            counter.getWordsMap().forEach((key, value) -> System.out.println(key + "\t" + value));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
