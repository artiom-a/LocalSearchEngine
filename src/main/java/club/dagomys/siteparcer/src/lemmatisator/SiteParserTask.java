package club.dagomys.siteparcer.src.lemmatisator;

import club.dagomys.siteparcer.src.dto.Link;
import club.dagomys.siteparcer.src.entity.Site;
import club.dagomys.siteparcer.src.exception.PageIndexingException;
import club.dagomys.siteparcer.src.exception.SiteIndexingException;
import club.dagomys.siteparcer.src.services.MainService;
import lombok.NoArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RecursiveTask;
import java.util.regex.Pattern;

@Component
@NoArgsConstructor
public class SiteParserTask extends RecursiveTask<Link> {
    private Link rootURL;
    private Site site;

    private MainService mainService;
    private static final Logger mainLogger = LogManager.getLogger(SiteParserTask.class);

    public SiteParserTask(Link link) throws PageIndexingException {
        this.rootURL = link;
    }

    public SiteParserTask(Link rootLink, MainService mainService, Site site) throws SiteIndexingException {
        this.rootURL = rootLink;
        this.mainService = mainService;
        this.site = site;
    }


    //TODO: 22.06.2022{
    // реализовать метод обхода сайта с сохранением ссылки, кода ответа и содержимого страницы
    // }
    @Override
    protected Link compute() {
        List<SiteParserTask> childParserList = new ArrayList<>();
        Link connLink = null;
        try {

            connLink = connectToLink(rootURL);
            for (Link child : connLink.getChildren()) {
                if (this.mainService.isIndexing()) {
                    SiteParserTask childParser = new SiteParserTask(child, mainService, site);
                    childParser.fork();
                    childParserList.add(childParser);
                } else throw new SiteIndexingException("Parsing is stopped " + site.getUrl());

            }

            for (SiteParserTask childTask : childParserList) {
                if (this.mainService.isIndexing()) {
                    childTask.compute();
                    childTask.join();
                } else throw new SiteIndexingException("Compute is stopped " + site.getUrl());
            }

        } catch (SiteIndexingException e) {
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
            Thread.sleep(100);
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
            mainService.getSiteService().saveAndFlush(site);
            mainLogger.error(e.getMessage());
        }
        return connectedLink;
    }

}
