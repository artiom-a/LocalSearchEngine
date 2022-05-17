package club.dagomys.siteparcer;

import club.dagomys.siteparcer.src.entity.Link;
import club.dagomys.siteparcer.src.SiteParser;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;

@Component
public class MyCommandLineRunner implements CommandLineRunner {
    @Override
    public void run(String... args) {
        System.out.println("Command line is running");
        try {
            final File siteMap = new File("src/main/java/output/siteMap.txt");
            final FileWriter fw = new FileWriter(siteMap);
            final String URL = "http://www.playback.ru/ ";

            Link mainLink = new Link(URL);
            ForkJoinPool siteMapPool = new ForkJoinPool(Runtime.getRuntime().availableProcessors());
            ForkJoinTask<Link> forkJoinTask = new SiteParser(mainLink);
            siteMapPool.invoke(forkJoinTask);

            fw.write(SiteParser.createSitemap(mainLink));

            fw.flush();
            fw.close();
            System.out.println("Command line runner is finished");
        } catch (IOException e) {
            System.out.println(e.getMessage());

        }
    }

}
