package club.dagomys.siteparcer.src.services;

import club.dagomys.siteparcer.src.entity.Page;
import club.dagomys.siteparcer.src.entity.Site;
import club.dagomys.siteparcer.src.repos.SiteRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
@Transactional
public class SiteService {
    private final Logger mainLogger = LogManager.getLogger(SiteService.class);
    @Autowired
    private SiteRepository siteRepository;

    @Autowired
    private PageService pageService;


    public CompletableFuture<Site> saveSite(Site site) {
        return CompletableFuture.completedFuture(siteRepository.save(site));
    }


    public void updateSite(Site site) {
        siteRepository
                .findByUrl(site.getUrl())
                .ifPresentOrElse(s -> {
                    s.setStatus(site.getStatus());
                    s.setUrl(site.getUrl());
                    s.setStatusTime(site.getStatusTime());
                    siteRepository.save(s);
                }, () -> siteRepository.save(site));
    }

    public Site getSite(String url) {
        Optional<Site> site = siteRepository.findByUrl(url);
        return site.orElseGet(Site::new);
    }

    public Site getSite(Integer siteId) {
        Optional<Site> siteOptional = siteRepository.findById(siteId);
        return siteOptional.orElse(null);
    }

    public Integer getSiteCount() {
        return (int) siteRepository.count();
    }

    public List<Site> getAllSites() {
        List<Site> sites = new ArrayList<>();
        siteRepository.findAll().forEach(sites::add);
        return sites;
    }

    public List<Page> findPageBySite(Site site) {
        return pageService.getPagesBySite(site);
    }

    public void deleteSite(Site site) {
        siteRepository.delete(site);
    }
}
