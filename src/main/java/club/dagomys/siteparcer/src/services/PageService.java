package club.dagomys.siteparcer.src.services;

import club.dagomys.siteparcer.src.entity.*;
import club.dagomys.siteparcer.src.repos.PageRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

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
    @Transactional
    public synchronized Page savePage(Page page) {
        return pageRepository.save(page);
    }

    public Page getPageById(Integer id) {
        return getAllPages().stream().filter(page -> page.getId() == id).findFirst().orElseThrow(NoSuchElementException::new);
    }

    public List<Page> getPagesBySite(Site site) {
        return pageRepository.getPageBySite(site).orElse(null);
    }

}
