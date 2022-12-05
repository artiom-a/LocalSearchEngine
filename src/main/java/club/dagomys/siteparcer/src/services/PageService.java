package club.dagomys.siteparcer.src.services;

import club.dagomys.siteparcer.src.entity.*;
import club.dagomys.siteparcer.src.repos.PageRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
public class PageService {
    private final Logger mainLogger = LogManager.getLogger(PageService.class);

    @Autowired
    private PageRepository pageRepository;


    public List<Page> getAllPages() {
        List<Page> allPages = new ArrayList<>();
        for (Page page : pageRepository.findAll()) {
            allPages.add(page);
        }
        return allPages;
    }

    @Async("taskExecutor")
    public CompletableFuture<Page> savePage(Page page) {
        return CompletableFuture.completedFuture(pageRepository.save(page));
    }


    public void updatePage(Page page) {
        pageRepository
                .findByRelPathAndSite(page.getRelPath(), page.getSite())
                .ifPresentOrElse(p -> {
                    p.setLink(page.getLink());
                    p.setSite(page.getSite());
                    p.setContent(page.getContent());
                    p.setStatusCode(page.getStatusCode());
                    p.setRelPath(page.getRelPath());
                    pageRepository.save(p);
                }, () -> pageRepository.save(page));
    }

    public Page getPageById(Integer id) {
        return getAllPages().stream().filter(page -> page.getId() == id).findFirst().orElseThrow(NoSuchElementException::new);
    }

    public List<Page> getPagesBySite(Site site) {
        mainLogger.info(pageRepository.getPageBySite(site));
        return pageRepository.getPageBySite(site).orElse(null);
    }

}
