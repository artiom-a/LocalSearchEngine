package club.dagomys.siteparcer.src.services;

import club.dagomys.siteparcer.src.entity.*;
import club.dagomys.siteparcer.src.repos.PageRepository;
import club.dagomys.siteparcer.src.repos.SiteRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

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

    @Async("taskExecutor")
    @Transactional
    public CompletableFuture<Page> savePage(Page page) {
        CompletableFuture<Page> completableFuture = new CompletableFuture<>();
        completableFuture.complete(pageRepository.saveAndFlush(page));
        return completableFuture;
    }


/*    @Async("taskExecutor")
    @Transactional
    public void updatePage(Page page) {
        mainLogger.warn(page);
        pageRepository
                .findByRelPathAndSite(page.getRelPath(), page.getSite())
                .ifPresentOrElse(p -> {
                    p.setLink(page.getLink());
                    p.setSite(page.getSite());
                    p.setContent(page.getContent());
                    p.setStatusCode(page.getStatusCode());
                    p.setRelPath(page.getRelPath());
                    pageRepository.saveAndFlush(p);
                }, () -> pageRepository.saveAndFlush(page));
    }*/
    @Async("taskExecutor")
    @Transactional
    public void updatePage(Page page) {
        Optional<Page> findPage = pageRepository.findByRelPathAndSite(page.getRelPath(), page.getSite());
        Optional<Site> findSite = siteRepository.findById(page.getSite().getId());
        if (findPage.isEmpty()) {
            mainLogger.info("saving page " + page);
            page.setSite(findSite.get());
            pageRepository.saveAndFlush(page);
        } else {
            Page p = findPage.get();
            p.setLink(page.getLink());
            p.setSite(findSite.get());
            p.setContent(page.getContent());
            p.setStatusCode(page.getStatusCode());
            p.setRelPath(page.getRelPath());
            mainLogger.info("update page " + p);
            pageRepository.saveAndFlush(p);
        }
    }

    public Page getPageById(Integer id) {
        return getAllPages().stream().filter(page -> page.getId() == id).findFirst().orElseThrow(NoSuchElementException::new);
    }

    public void deletePage(Page page) {
        pageRepository.delete(page);
    }

    public List<Page> getPagesBySite(Site site) {
        mainLogger.info(pageRepository.getPageBySite(site));
        return pageRepository.getPageBySite(site).orElse(null);
    }

    public Page getByRelPathAndSite(String path, Site site) {
        Optional<Page> findPage = pageRepository.findByRelPathAndSite(path, site);
        if (findPage.isPresent()) {
            return findPage.get();
        } else {
            mainLogger.error(new Exception("page not found ex!!!"));
            return null;
        }
    }

    public void deleteAll(List<Page> pageList) {
        pageRepository.deleteAll(pageList);
    }

}
