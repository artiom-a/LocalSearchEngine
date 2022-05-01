package club.dagomys.siteparcer.src;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.RecursiveTask;
import java.util.regex.Pattern;

public class SiteParser extends RecursiveTask<Link> {
    private Link URL;

    public SiteParser(Link URL) throws IOException {
        this.URL = URL;
    }

    @Override
    protected Link compute() {
        List<SiteParser> childParserList = new ArrayList<>();
        List<Link> childList = new ArrayList<>();
        try {
            Thread.sleep(150);
            Document siteFile = Jsoup
                    .connect(URL.getValue())
                    .userAgent("Mozilla")

                    .get();
            Elements siteElements = siteFile.select("a[href]");
            if (siteElements.isEmpty()) {
                return null;
            } else {
                siteElements.stream().map(link -> link.attr("abs:href")).forEachOrdered(link -> {
                    if (urlChecker(link)) {
                        Link child = new Link(link);
                        URL.addChild(child);
                    }
                });
                for (Link child : URL.getChildren()) {
                    SiteParser childParser = new SiteParser(child);
                    childParser.fork();
                    childParserList.add(childParser);
                }
                for (SiteParser childTask : childParserList) {
                    System.out.println("\t\t" + childTask.URL);
                    childList.add(childTask.join());
                }
            }
        } catch (InterruptedException | IOException e) {
            System.out.println(e.getMessage());
        }
        return URL;
    }

    private boolean urlChecker(String url) {
        Pattern urlPattern = Pattern.compile("(https?:\\/\\/(?:www\\.|(?!www))[a-zA-Z0-9][a-zA-Z0-9-]+[a-zA-Z0-9]\\.[^\\s]{2,}|www\\.[a-zA-Z0-9][a-zA-Z0-9-]+[a-zA-Z0-9]\\.[^\\s]{2,}|https?:\\/\\/(?:www\\.|(?!www))[a-zA-Z0-9]+\\.[^\\s]{2,}|www\\.[a-zA-Z0-9]+\\.[^\\s]{2,})");
        Pattern patternRootDomain = Pattern.compile("^" + URL.getValue());
        Pattern notFile = Pattern.compile("([^\\s]+((jpg|png|gif|bmp|pdf))$)");
        Pattern notAnchor = Pattern.compile("#([\\w\\-]+)?$");
        return
                urlPattern.matcher(url).find() &
                        patternRootDomain.matcher(url).lookingAt() &
                        !notFile.matcher(url).find() &
                        !notAnchor.matcher(url).find();
    }

    public static String createSitemap(Link node) {
        String tabs = String.join("", Collections.nCopies(node.getLayer(), "\t"));
        StringBuilder result = new StringBuilder(tabs + node.getValue());
        node.getChildren().forEach(child -> {
            result.append("\n").append(createSitemap(child));
        });
        return result.toString();
    }
}
