package club.dagomys.siteparcer.src.services;

import club.dagomys.siteparcer.src.entity.*;
import club.dagomys.siteparcer.src.repos.PageRepository;
import club.dagomys.siteparcer.src.repos.SiteRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
            return savePage(page);
        } else {
            Page p = findPage.get();
            p.setLink(page.getLink());
            p.setSite(page.getSite());
            p.setContent(page.getContent());
            p.setStatusCode(page.getStatusCode());
            p.setRelPath(page.getRelPath());
            mainLogger.info("update page " + p);
            return savePage(p);
        }
    }

    public Page getPageById(Integer id) {
        return getAllPages().stream().filter(page -> page.getId() == id).findFirst().orElseThrow(NoSuchElementException::new);
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
            mainLogger.error(new Exception("page not found ex!!!"));
            return null;
        }
    }

    public void deleteAll(List<Page> pageList) {
        pageRepository.deleteAll(pageList);
    }

}
