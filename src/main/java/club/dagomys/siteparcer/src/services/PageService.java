package club.dagomys.siteparcer.src.services;

import club.dagomys.siteparcer.src.entity.*;
import club.dagomys.siteparcer.src.repos.PageRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

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

    public Page savePage(Page page) {
        return pageRepository.save(page);
    }

    public Page getPageById(Integer id) {
        return getAllPages().stream().filter(page -> page.getId() == id).findFirst().orElseThrow(NoSuchElementException::new);
    }

    public void startSiteParse(String url) {
        SiteParserRunner siteParser = new SiteParserRunner(url, this);
        if (siteParser.getStatus()) {
            mainLogger.warn("SiteParser is running!");
        } else {

            siteParser.run();
        }

    }



    public void insertToDatabase(Link link) {
        Page root = new Page(link.getRelUrl());
        root.setStatusCode(link.getStatusCode());
        root.setContent(link.getHtml());
        pageRepository.save(root);
        link.getChildren().forEach(this::insertToDatabase);
    }

    private String createSitemap(Link node) {
        String tabs = String.join("", Collections.nCopies(node.getLayer(), "\t"));
        StringBuilder result = new StringBuilder(tabs + node.getRelUrl());
        node.getChildren().forEach(child -> {
            result.append("\n").append(createSitemap(child));
        });
        return result.toString();
    }

}
