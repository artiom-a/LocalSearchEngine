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
                    s.setLastError(site.getLastError());
                    siteRepository.save(s);
                }, () -> siteRepository.save(site));
    }


    public Optional<Site> getSite(String url) {
        return siteRepository.findByUrl(url);
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

    public void deleteSite(Site site) {
        siteRepository.delete(site);
    }
}
