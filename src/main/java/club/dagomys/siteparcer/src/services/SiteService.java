package club.dagomys.siteparcer.src.services;

import club.dagomys.siteparcer.src.entity.Site;
import club.dagomys.siteparcer.src.repos.SiteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class SiteService {

    @Autowired
    private SiteRepository siteRepository;


    public Site saveSite(Site site) {
        return siteRepository.save(site);
    }

    public Site getSite(String url){
        return siteRepository.findByUrl(url);
    }

    public Site getSite(Integer siteId){
        Optional<Site> siteOptional = siteRepository.findById(siteId);
        return siteOptional.orElse(null);
    }

    public Integer getSiteCount(){
        return (int) siteRepository.count();
    }

    public List<Site> gettAllSites(){
        List<Site> sites = new ArrayList<>();
        siteRepository.findAll().forEach(sites::add);
        return sites;
    }
}
