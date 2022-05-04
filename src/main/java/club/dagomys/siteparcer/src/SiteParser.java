package club.dagomys.siteparcer.src;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.RecursiveTask;
import java.util.regex.Pattern;

public class SiteParser extends RecursiveTask<Link> {
    private Link rootURL;

    public SiteParser(Link URL) throws IOException {
        this.rootURL = URL;
    }

    @Override
    protected Link compute() {
        List<SiteParser> childParserList = new ArrayList<>();
        List<Link> childList = new ArrayList<>();
        try {
            Thread.sleep(150);
            Document siteFile = Jsoup
                    .connect(rootURL.getValue())
                    .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                    .referrer("http://www.google.com")
                    .get();
/*            File file = new File("src/main/java/output/" + siteFile.title() + ".html");
            FileWriter fw = new FileWriter(file);
            Document pageDoc = siteFile.clone().normalise();
            fw.write(pageDoc.outerHtml());
            fw.flush();
            fw.close();*/
            int status = siteFile.connection().response().statusCode();
            Elements siteElements = siteFile.select("a[href]");
            if (siteElements.isEmpty()) {
                return null;
            } else {
                siteElements.forEach(link -> {
                    if (urlChecker(link.absUrl("href"))) {
                        Link child = new Link(link.absUrl("href"));
                        Link relChild = new Link(link.attr("href"));
                        rootURL.setStatusCode(status);
                        rootURL.getRelChildren().add(relChild);
                        rootURL.addChild(child);
                    }
                });
                for (Link child : rootURL.getChildren()) {
                    SiteParser childParser = new SiteParser(child);
                    childParser.fork();
                    childParserList.add(childParser);
                }
                for (SiteParser childTask : childParserList) {
                    System.out.println("\t\t" + childTask.rootURL);
                    System.out.println(childTask.rootURL.getRelChildren());
                    childList.add(childTask.join());
                }
            }
        } catch (InterruptedException | IOException e) {
            System.out.println(e.getMessage());
        }
        return rootURL;
    }

    private boolean urlChecker(String url) {
        Pattern urlPattern = Pattern.compile("(https?:\\/\\/(?:www\\.|(?!www))[a-zA-Z0-9][a-zA-Z0-9-]+[a-zA-Z0-9]\\.[^\\s]{2,}|www\\.[a-zA-Z0-9][a-zA-Z0-9-]+[a-zA-Z0-9]\\.[^\\s]{2,}|https?:\\/\\/(?:www\\.|(?!www))[a-zA-Z0-9]+\\.[^\\s]{2,}|www\\.[a-zA-Z0-9]+\\.[^\\s]{2,})");
        Pattern patternRootDomain = Pattern.compile("^" + rootURL.getValue());
        Pattern file = Pattern.compile("([^\\s]+((jpg|png|gif|bmp|pdf|JPG))$)");
        Pattern anchor = Pattern.compile("#([\\w\\-]+)?$");
        return
                urlPattern.matcher(url).find() &
                        patternRootDomain.matcher(url).lookingAt() &
                        !file.matcher(url).find() &
                        !anchor.matcher(url).find();
    }

    public static String createSitemap(Link node) {
        String tabs = String.join("", Collections.nCopies(node.getLayer(), "\t"));
        StringBuilder result = new StringBuilder(tabs + node.getValue());
        node.getRelChildren().forEach(child -> {
            result.append("\n").append(createSitemap(child.getAbsLink()));
        });

//        node.getChildren().forEach(child -> {
//            result.append("\n").append(createSitemap(child));
//        });
        return result.toString();
    }
}
