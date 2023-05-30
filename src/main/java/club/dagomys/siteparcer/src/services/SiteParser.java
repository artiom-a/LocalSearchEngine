package club.dagomys.siteparcer.src.services;

import club.dagomys.siteparcer.src.entity.Link;
import club.dagomys.siteparcer.src.entity.Site;
import lombok.NoArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
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
        Link connLink = null;
        try {
            connLink = connectToLink(rootURL);
            for (Link child : connLink.getChildren()) {
                SiteParser childParser = new SiteParser(child, mainService, site);
                childParser.fork();
                childParserList.add(childParser);
            }
            for (SiteParser childTask : childParserList) {
                mainLogger.info("\t\t" + childTask.compute());
                childList.add(childTask.join());
            }
        } catch (IOException e) {
            mainLogger.error(e.getMessage());
        }
        return connLink;
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

    public Link connectToLink(Link connectedLink) {
        try {
            Thread.sleep(120);
            Document siteFile = Jsoup
                    .connect(connectedLink.getValue())
                    .userAgent(mainService.getAppConfig().getUserAgent())
                    .referrer("http://www.google.com")
                    .ignoreHttpErrors(false)
                    .get();
            Elements siteElements = siteFile.select("a[href]");
            int status = siteFile.connection().response().statusCode();
            siteElements.forEach(link -> {
                String absolutURL = link.absUrl("href");
                StringBuilder relativeURL = new StringBuilder(absolutURL.replace(site.getUrl(), ""));
                if (relativeURL.indexOf("/", 0) != 0) {
                    relativeURL.insert(0, "/");

                }
                connectedLink.setHtml(siteFile.outerHtml());
                connectedLink.setStatusCode(status);
                connectedLink.setSite(site);
                if (urlChecker(absolutURL)) {
                    Link child = new Link(absolutURL);
                    child.setSite(site);
                    connectedLink.addChild(child, relativeURL.toString());
                }

            });
        } catch (Exception e) {
            this.site.setLastError(e.getMessage());
            mainService.getSiteService().saveOrUpdate(site);
            mainLogger.error(e.getMessage());
        }
        return connectedLink;
    }

}
