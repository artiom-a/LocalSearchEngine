package club.dagomys.siteparcer;

import club.dagomys.siteparcer.src.Link;
import club.dagomys.siteparcer.src.SiteParser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.parser.Tag;
import org.jsoup.select.Evaluator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
@SpringBootApplication
public class MainSiteparcer {

    public static void main(String[] args) throws IOException {
        SpringApplication.run(MainSiteparcer.class, args);
        final File siteMap = new File("src/main/java/output/siteMap.txt");
        final FileWriter fw = new FileWriter(siteMap);
        final String URL = "http://www.playback.ru/        ";

        Link mainLink = new Link(URL);
        ForkJoinPool siteMapPool = new ForkJoinPool(Runtime.getRuntime().availableProcessors());
        ForkJoinTask<Link> forkJoinTask = new SiteParser(mainLink);
        siteMapPool.invoke(forkJoinTask);

        fw.write(SiteParser.createSitemap(mainLink));

        fw.flush();
        fw.close();
    }
}
