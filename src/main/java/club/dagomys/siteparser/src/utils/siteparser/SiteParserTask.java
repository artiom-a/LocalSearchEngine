package club.dagomys.siteparser.src.utils.siteparser;

import club.dagomys.siteparser.src.dto.Link;
import club.dagomys.siteparser.src.entity.Site;
import club.dagomys.siteparser.src.services.IndexingService;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.util.concurrent.RecursiveTask;
import java.util.regex.Pattern;

@Component
@NoArgsConstructor
@Slf4j
public class SiteParserTask extends RecursiveTask<Link> {
    private Link rootLink;
    private Site site;
    private IndexingService indexingService;

    public SiteParserTask(Link rootLink, IndexingService indexingService, Site site){
        this.rootLink = rootLink;
        this.indexingService = indexingService;
        this.site = site;
    }


    //TODO: 22.06.2022{
    // реализовать метод обхода сайта с сохранением ссылки, кода ответа и содержимого страницы
    // }
    @Override
    protected Link compute() {
        Link connLink = connectToLink(rootLink);
        for (Link child : connLink.getChildren()) {
            if (this.indexingService.getIsIndexing().get()) {
                SiteParserTask childParser = new SiteParserTask(child, indexingService, site);
                childParser.fork();
                childParser.join();

            }

        }
        log.info(String.valueOf(connLink));
        return connLink;
    }

    public Link connectToLink(Link connectedLink) {
        try {
            Document siteFile = Jsoup
                    .connect(connectedLink.getValue())
                    .userAgent(indexingService.getAppConfig().getUserAgent())
                    .referrer(indexingService.getAppConfig().getReferer())
                    .followRedirects(true)
                    .ignoreHttpErrors(false)
                    .ignoreContentType(true)
                    .timeout(10000)
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
            site.setLastError(e.toString());
            indexingService.getSiteRepository().saveAndFlush(site);
            log.error(e.toString());
        }
        return connectedLink;
    }


    private boolean urlChecker(String url) {
        Pattern urlPattern = Pattern.compile("(https?://(?:www\\.|(?!www))[a-zA-Z0-9][a-zA-Z0-9-]+[a-zA-Z0-9]\\.\\S{2,}|www\\.[a-zA-Z0-9][a-zA-Z0-9-]+[a-zA-Z0-9]\\.\\S{2,}|https?://(?:www\\.|(?!www))[a-zA-Z0-9]+\\.\\S{2,}|www\\.[a-zA-Z0-9]+\\.\\S{2,})");
        Pattern patternRootDomain = Pattern.compile("^" + rootLink.getSite().getUrl());
        Pattern file = Pattern.compile("(\\S+((jpg|png|gif|bmp|pdf|ics|xml|jpeg))$)");
        Pattern anchor = Pattern.compile("\\S+#([\\w\\-]+)?$");
        return
                urlPattern.matcher(url).find() &
                        patternRootDomain.matcher(url).lookingAt() &
                        !file.matcher(url.toLowerCase()).find() &
                        !anchor.matcher(url).find();
    }

}
