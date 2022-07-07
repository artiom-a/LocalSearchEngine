import club.dagomys.lemmatisator.scr.LemmaCounter;


import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.*;

class LemmaCounterTest {
    private LemmaCounter englishLemmaCounter;
    private LemmaCounter russianLemmaCounter;
    private LemmaCounter otherLangLemmaCounter;

    @BeforeEach
    void setUp() {
        try {
            String englishText = "I am sure learning foreign languages is very important nowadays. People start learning a foreign language, because they want to have a better job, a possibility to study abroad or take part in international conferences. People want to have a possibility to get a higher education abroad or even start their career there. The most popular among foreign languages are English, German, Italian, French and Spanish.";
            englishLemmaCounter = new LemmaCounter(englishText);
            String russianText = "Повторное появление леопарда в Осетии позволяет предположить, что леопард постоянно обитает в некоторых районах Северного Кавказа.";
            russianLemmaCounter = new LemmaCounter(russianText);
            String french = "Ma ville\n" +
                    "J'habite une belle ville dans le nord de la France. Il y a un quartier très agréable pour aller se balader, lorsqu'il fait beau. Il est possible de faire du vélo dans un parc autour d'une citadelle fortifiée.";
            otherLangLemmaCounter = new LemmaCounter(french);
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }

    @Test
    @DisplayName("Поиск лемм в русскоязычном тексте")
    void getWordsMap_RussianLemmas() {
        long russianLemmasCount = russianLemmaCounter.getWordsMap().entrySet().size();
        assertEquals(11, russianLemmasCount);
    }

    @Test
    @DisplayName("Поиск лемм в англоязычном тексте")
    void getWordsMap_EnglishLemmas(){
        long englishLemmasCount = englishLemmaCounter.getWordsMap().entrySet().size();
        assertEquals(40, englishLemmasCount);
    }

    @Test
    @DisplayName("Поиск лемм в тексте с неопределенным языком")
    void getWordsMap_OtherLangLemmas(){
        assertNull(otherLangLemmaCounter.getWordsMap());
    }


    @AfterEach
    void tearDown() {
    }
}