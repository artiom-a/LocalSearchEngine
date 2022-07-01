package club.dagomys.lemmatisator.scr;

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
            String text = "Гроза\n" +
                    "В июле начались грозы — раскатистые, воронежские, страшные. После пары дней тяжёлой неподвижной жары вдруг приходил с воды плотный холодный ветер, грубо ерошил прибрежную зелень, упругой волной прокатывался по улицам, грохал створками ворот, пробовал на прочность кровельное железо. Хозяйки ругались на чём свет стоит, стучали деревянными ставнями, ахая, тянули с верёвок хлопотливо рвущееся из рук бельё. Обмирая от предчувствия, метались заполошные огоньки лампадок, пело и дрожало оконное стекло.\n" +
                    "\n" +
                    "Вслед за ветром приходили тучи.\n" +
                    "\n" +
                    "За пару минут город темнел, будто зажмуривался, и на горизонте, нестерпимо яркая на серо-лиловом фоне, вставала, ветвясь, первая громадная молния. Дома становились ниже, приседали на корточки, зажимали в счастливом ужасе уши. И через секунду-другую громко, с хрустом разрывалось в небе сырое натянутое полотно.\n" +
                    "\n" +
                    "И ещё раз, и ещё.\n" +
                    "\n" +
                    "А потом на обмерший Воронеж обрушивалась вода. Рыча, она бросалась на крыши, на подоконники, хрипела в водосточных трубах, рыскала, нападала — и по вершинам деревьев видно было, как она шла. По мощёным улицам в центре текло, водоворотами закручиваясь на перекрёстках, окраины заплывали живой жирной грязью. Квартирная хозяйка, крестясь, обходила комнаты, бормоча не то молитвы, не то заклинания, и свечной огонёк пытался вырваться из-под её трясущейся ладони. Трусила. Мать тоже, как все, захлопывала окна, накидывала платок на хрупкие плечи, но — Саня видел — не боялась совершенно. Была красивая, холодная, неживая — как всегда. Сам он грозу обожал и после первого же залпа небесной шрапнели выскакивал во двор, радуясь тому, как вскипают лужи, кружится голова и прилипает под мышками и на спине ледяная, с каждой секундой тяжелеющая рубаха.\n" +
                    "\n" +
                    "Гроза рифмовалась с любовью. С этим летом.\n" +
                    "\n" +
                    "Лучшего лета Саня ещё не знал.";
            String text2 = "Повторное появление леопарда в Осетии позволяет предположить, что леопард постоянно обитает в некоторых районах Северного Кавказа.";

            String englishText = "I am sure learning foreign languages is very important nowadays. People start learning a foreign language, because they want to have a better job, a possibility to study abroad or take part in international conferences. People want to have a possibility to get a higher education abroad or even start their career there. The most popular among foreign languages are English, German, Italian, French and Spanish.\n" +
                    "\n" +
                    "I have chosen English as a foreign language, because it is the most widespread language on the Earth. About one billion people speak or understand English. English is the language of international communication in many areas of life: trade, tourism and sport. The latest results of scientific investigations are also translated into English. Many books of the best modern writers and poets are translated into English. Sometimes, it is the only way to read and understand the latest works of foreign authors, Japanese or Turkish, for example.\n" +
                    "\n" +
                    "Language is a means of communication. We learn it in order to find new friends abroad and get acquainted with other cultures. Some of my friends have already moved to the USA and Canada. They often write me letters in English and I am glad that I can understand them without anybody's help. This communication helps me to learn new English words and master my speaking skills.";
            LemmaCounter counter = new LemmaCounter(text);
            counter.getWordsMap().forEach((key, value) -> System.out.println(key + "\t" + value));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
