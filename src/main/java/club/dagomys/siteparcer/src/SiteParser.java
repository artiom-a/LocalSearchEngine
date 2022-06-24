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
import java.util.*;
import java.util.concurrent.RecursiveTask;
import java.util.regex.Pattern;

public class SiteParser extends RecursiveTask<Link> {
    private Link rootURL;
    private static Logger mainLogger = MainLog4jLogger.getIstance();

    public SiteParser(Link URL) throws IOException {
        this.rootURL = URL;
    }

    //TODO: 22.06.2022{
    // реализовать метод обхода сайта с сохранением ссылки, кода ответа и содержимого страницы
    // }
    @Override
    protected Link compute() {
        List<SiteParser> childParserList = new ArrayList<>();
        List<Link> childList = new ArrayList<>();
        try {
            Thread.sleep(120);
            Document siteFile = Jsoup
                    .connect(rootURL.getValue())
                    .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                    .referrer("http://www.google.com")
                    .get();

            Elements siteElements = siteFile.select("a[href]");
            if (siteElements.isEmpty()) {
                mainLogger.info("Site element is null");
                return null;
            } else {
                int status = siteFile.connection().response().statusCode();
                siteElements.forEach(link -> {
                    String absolutURL = link.absUrl("href");
                    String relativeURL = link.attr("href");
                    rootURL.setHtml(siteFile.outerHtml());
                    rootURL.setStatusCode(status);
                    if (urlChecker(absolutURL)) {
                        Link child = new Link(absolutURL);
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
        } catch (IOException | InterruptedException e) {
            mainLogger.error(e.getMessage());
        }
        return rootURL;
    }

    private boolean urlChecker(String url) {
        Pattern urlPattern = Pattern.compile("(https?:\\/\\/(?:www\\.|(?!www))[a-zA-Z0-9][a-zA-Z0-9-]+[a-zA-Z0-9]\\.[^\\s]{2,}|www\\.[a-zA-Z0-9][a-zA-Z0-9-]+[a-zA-Z0-9]\\.[^\\s]{2,}|https?:\\/\\/(?:www\\.|(?!www))[a-zA-Z0-9]+\\.[^\\s]{2,}|www\\.[a-zA-Z0-9]+\\.[^\\s]{2,})");
        Pattern patternRootDomain = Pattern.compile("^" + rootURL.getValue());
        Pattern file = Pattern.compile("([^\\s]+((jpg|png|gif|bmp|pdf|JPG|ics))$)");
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
