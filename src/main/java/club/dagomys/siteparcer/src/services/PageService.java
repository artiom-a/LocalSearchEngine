package club.dagomys.siteparcer.src.services;

import club.dagomys.siteparcer.src.entity.*;
import club.dagomys.siteparcer.src.entity.request.URLRequest;
import club.dagomys.siteparcer.src.entity.response.Response;
import club.dagomys.siteparcer.src.exception.PageIndexingException;
import club.dagomys.siteparcer.src.repos.PageRepository;
import club.dagomys.siteparcer.src.repos.SiteRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.plugins.validation.constraints.Required;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class PageService {
    private final Logger mainLogger = LogManager.getLogger(PageService.class);

    @Autowired
    private PageRepository pageRepository;

    @Autowired
    private SiteRepository siteRepository;


    public List<Page> getAllPages() {
        return new ArrayList<>(pageRepository.findAll());
    }

    public Page savePage(Page page) {
        return pageRepository.saveAndFlush(page);
    }

    public Page saveOrUpdate(Page page) {
        Optional<Page> findPage = pageRepository.findByRelPathAndSite(page.getRelPath(), page.getSite()).stream().findFirst();
        if (findPage.isEmpty()) {
            mainLogger.info("saving page " + page);
            page.setSite(page.getSite());
            return pageRepository.saveAndFlush(page);
        } else {
            Page p = findPage.get();
            p.setLink(page.getLink());
            p.setSite(page.getSite());
            p.setContent(page.getContent());
            p.setStatusCode(page.getStatusCode());
            p.setRelPath(page.getRelPath());
            mainLogger.info("update page " + p);
            return pageRepository.saveAndFlush(p);
        }
    }

    public Page getPageById(Integer id) {
        return pageRepository.findAll().stream().filter(page -> Objects.equals(page.getId(), id)).findFirst().orElseThrow(NoSuchElementException::new);
    }

    public void deletePage(Page page) {
        pageRepository.delete(page);
    }

    public List<Page> getPagesBySite(Site site) {
        return pageRepository.getPageBySite(site).get();
    }

    public Page getByRelPathAndSite(String path, Site site) {
        Optional<Page> findPage = pageRepository.findByRelPathAndSite(path, site);
        if (findPage.isPresent()) {
            return findPage.get();
        } else {
            mainLogger.error(new Exception("page not found exception!!!"));
            return null;
        }
    }

    public Response reindexPage(URLRequest URL, @Required Errors error) {
        Response response = new Response();
        List<Site> siteList = siteRepository.findAll();
        Optional<Site> site = Optional.empty();
        Optional<Page> page = Optional.of(new Page());

        Link rootLink = new Link(URL.getPath());
        try {
            for (Site s : siteList) {
                if (s.getStatus() == SiteStatus.INDEXING) {
                   throw new PageIndexingException("Сайт " + s.getUrl() + " в процессе индексации");
                }
                if (rootLink.getValue().contains(s.getUrl())) {
                    site = Optional.of(s);
                    mainLogger.info(site);
                }
            }
            if (site.isPresent()) {
                if (!error.hasErrors()) {
                    String relativeURL = rootLink.getValue().replace(site.get().getUrl(), "");
                    page.get().setRelPath(relativeURL);
                    page.get().setSite(site.get());
                    try {
                        Document pageFile = Jsoup
                                .connect(site.get().getUrl() + page.get().getRelPath())
                                .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                                .referrer("http://www.google.com")
                                .ignoreHttpErrors(false)
                                .get();
                        page.get().setContent(pageFile.outerHtml());
                        page.get().setStatusCode(pageFile.connection().response().statusCode());
                        saveOrUpdate(page.get());
                    } catch (Exception e) {
                        mainLogger.error(e.getMessage());
                    }
                    site.get().setStatusTime(LocalDateTime.now());
                    siteRepository.save(site.get());
                    response.setResult(true);
                } else {
                    response.setError(Objects.requireNonNull(error.getFieldError()).getDefaultMessage());
                }
            } else {
                response.setError("Данная страница находится за пределами сайтов, указанных в конфигурационном файле");
                throw new PageIndexingException("Данная страница находится за пределами сайтов, указанных в конфигурационном файле");
            }
        } catch (Exception e) {
            response.setResult(false);
            response.setError(e.getMessage());
            mainLogger.error(e.getMessage());
        } catch (PageIndexingException e) {
            mainLogger.info(e.getMessage());
        }
        return response;
    }

    public void deleteAll(List<Page> pageList) {
        pageRepository.deleteAll(pageList);
    }

}
