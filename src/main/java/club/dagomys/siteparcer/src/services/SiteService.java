package club.dagomys.siteparcer.src.services;

import club.dagomys.siteparcer.src.entity.Page;
import club.dagomys.siteparcer.src.entity.Site;
import club.dagomys.siteparcer.src.repos.SiteRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SiteService {
    private final Logger mainLogger = LogManager.getLogger(SiteService.class);
    @Autowired
    private SiteRepository siteRepository;

    @Autowired
    private PageService pageService;


    public Site saveSite(Site site) {
        return siteRepository.saveAndFlush(site);
    }

    public void saveOrUpdate(Site site) {
        siteRepository
                .findByUrl(site.getUrl())
                .ifPresentOrElse(s -> {
                    s.setName(site.getName());
                    s.setStatus(site.getStatus());
                    s.setUrl(site.getUrl());
                    s.setStatusTime(site.getStatusTime());
                    s.setRootLink(site.getRootLink());
                    saveSite(s);
                }, () -> saveSite(site));
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
        return siteRepository.findAll();
    }

    public List<Page> findPageBySite(Site site) {
        return pageService.getPagesBySite(site);
    }

    public void deleteSite(Site site) {
        siteRepository.delete(site);
    }
}
