package club.dagomys.siteparcer.src.services;

import club.dagomys.siteparcer.src.entity.Link;
import club.dagomys.siteparcer.src.entity.Site;
import lombok.NoArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.RecursiveTask;
import java.util.regex.Pattern;

@Component
@NoArgsConstructor
public class SiteParser extends RecursiveTask<Link> {
    private Link rootURL;
    private Site site;

    private MainService mainService;
    private static final Logger mainLogger = LogManager.getLogger(SiteParser.class);

    public SiteParser(Link link) throws IOException {
        this.rootURL = link;
    }

    public SiteParser(Link rootLink, MainService mainService, Site site) throws IOException {
        this.rootURL = rootLink;
        this.mainService = mainService;
        this.site = site;
//        mainLogger.info(site);
//        rootURL.setSite());
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
                    .ignoreHttpErrors(true)
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
                    rootURL.setSite(site);
                    if (urlChecker(absolutURL)) {
                        Link child = new Link(absolutURL);
                        rootURL.addChild(child, relativeURL);
                    }

                });
                for (Link child : rootURL.getChildren()) {
                    SiteParser childParser = new SiteParser(child, mainService, site);
                    childParser.fork();
                    childParserList.add(childParser);
                }
                for (SiteParser childTask : childParserList) {
                    mainLogger.info("\t\t" + childTask.compute());
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

}
