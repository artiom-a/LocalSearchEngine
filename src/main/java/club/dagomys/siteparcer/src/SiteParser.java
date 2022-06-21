package club.dagomys.siteparcer.src;

import club.dagomys.siteparcer.src.entity.Link;
import club.dagomys.siteparcer.src.entity.MainLog4jLogger;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.RecursiveTask;
import java.util.regex.Pattern;

public class SiteParser extends RecursiveTask<Link> {
    private Link rootURL;
    private static Logger mainLogger = MainLog4jLogger.getIstance();

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
            int status = siteFile.connection().response().statusCode();

            Elements siteElements = siteFile.select("a[href]");
            if (siteElements.isEmpty()) {
                mainLogger.info("Site element is null");
                return null;
            } else {
                siteElements.forEach(link -> {
                    String absolutURL = link.absUrl("href");
                    String relativeURL = link.attr("href");
                    if (urlChecker(absolutURL)) {
                        Link child = new Link(absolutURL);

                        try {
//                            final File siteMap = new File("src/main/java/output/" + siteFile.title().replaceAll("\\/","") +".html");
//                            final FileWriter fw = new FileWriter(siteMap);
//                            mainLogger.warn(siteFile.connection().get());
                            child.setHtml(siteFile.connection().get().outerHtml());
                            child.setStatusCode(status);
//                            fw.write(siteFile.connection().get().outerHtml());

//                            fw.flush();
//                            fw.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        rootURL.addChild(child, relativeURL);
                    }
                });
                for (Link child : rootURL.getChildren()) {
                    SiteParser childParser = new SiteParser(child);
                    childParser.fork();
                    childParserList.add(childParser);
                }
                for (SiteParser childTask : childParserList) {
                    System.out.println("\t\t" + childTask.compute());
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

    private String createRelLink(String absLink) {

        return absLink.replaceAll(rootURL.getValue(), absLink);
    }
}
