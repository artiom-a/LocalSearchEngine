package club.dagomys.siteparcer.src.utils.siteparser;

import club.dagomys.siteparcer.src.dto.Link;
import club.dagomys.siteparcer.src.entity.Site;
import club.dagomys.siteparcer.src.exception.SiteIndexingException;
import club.dagomys.siteparcer.src.services.IndexingService;
import lombok.NoArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.util.concurrent.RecursiveTask;
import java.util.regex.Pattern;

@Component
@NoArgsConstructor
public class SiteParserTask extends RecursiveTask<Link> {
    private Link rootLink;
    private Site site;
    private IndexingService indexingService;
    private static final Logger mainLogger = LogManager.getLogger(SiteParserTask.class);


    public SiteParserTask(Link rootLink, IndexingService indexingService, Site site) throws SiteIndexingException {
        this.rootLink = rootLink;
        this.indexingService = indexingService;
        this.site = site;
    }


    //TODO: 22.06.2022{
    // реализовать метод обхода сайта с сохранением ссылки, кода ответа и содержимого страницы
    // }
    @Override
    protected Link compute() {
        Link connLink = null;
        try {
            connLink = connectToLink(rootLink);
            for (Link child : connLink.getChildren()) {
                if (this.indexingService.getIsIndexing().get()) {
                    SiteParserTask childParser = new SiteParserTask(child, indexingService, site);
                    childParser.fork();
                    childParser.join();
                } else throw new SiteIndexingException("Parsing is stopped " + site.getUrl());
            }
        } catch (SiteIndexingException e) {
            mainLogger.error(e.getMessage());
            site.setLastError(e.getMessage());
        }
        mainLogger.error(connLink);
        return connLink;
    }

    public Link connectToLink(Link connectedLink) {
        try {
            Thread.sleep(100);
            Document siteFile = Jsoup
                    .connect(connectedLink.getValue())
                    .userAgent(indexingService.getAppConfig().getUserAgent())
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

                if (connectedLink.getRelUrl() == null) {
                    connectedLink.setRelUrl("/");
                }
                if (urlChecker(absolutURL)) {
                    Link child = new Link(absolutURL);
                    child.setSite(site);
                    connectedLink.addChild(child, relativeURL.toString());
                }

            });
        } catch (Exception e) {
            site.setLastError(e.getMessage());
            indexingService.getSiteRepository().saveAndFlush(site);
            mainLogger.error(e.getMessage());
        }
        return connectedLink;
    }


    private boolean urlChecker(String url) {
        Pattern urlPattern = Pattern.compile("(https?://(?:www\\.|(?!www))[a-zA-Z0-9][a-zA-Z0-9-]+[a-zA-Z0-9]\\.\\S{2,}|www\\.[a-zA-Z0-9][a-zA-Z0-9-]+[a-zA-Z0-9]\\.\\S{2,}|https?://(?:www\\.|(?!www))[a-zA-Z0-9]+\\.\\S{2,}|www\\.[a-zA-Z0-9]+\\.\\S{2,})");
        Pattern patternRootDomain = Pattern.compile("^" + rootLink.getSite().getUrl());
        Pattern file = Pattern.compile("(\\S+((jpg|png|gif|bmp|pdf|JPG|ics|xml))$)");
        Pattern anchor = Pattern.compile("\\S+#([\\w\\-]+)?$");
        return
                urlPattern.matcher(url).find() &
                        patternRootDomain.matcher(url).lookingAt() &
                        !file.matcher(url).find() &
                        !anchor.matcher(url).find();
    }

}
