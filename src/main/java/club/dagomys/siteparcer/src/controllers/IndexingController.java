package club.dagomys.siteparcer.src.controllers;

import club.dagomys.siteparcer.src.entity.Site;
import club.dagomys.siteparcer.src.entity.request.URLRequest;
import club.dagomys.siteparcer.src.services.MainService;
import club.dagomys.siteparcer.src.services.SearchIndexService;
import club.dagomys.siteparcer.src.services.SiteService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;

@Controller
public class IndexingController {
    private final Logger mainLogger = LogManager.getLogger(IndexingController.class);

    @Autowired
    private SearchIndexService searchIndexService;

    @Autowired
    private MainService mainService;

    @Autowired
    private SiteService siteService;

    @GetMapping(value = {"/startCrawler"})
    public String startCrawler(Model model) {
        mainService.startIndexingAllSites();
        return "redirect:/lemmas";
    }

    @PostMapping("/addUrl")
    public String addUrl(@Valid @ModelAttribute("URL") URLRequest URL, Errors errors, Model model) {
        if (!errors.hasErrors()) {
            mainLogger.info(URL);
            Site site = mainService.startSiteParse(URL.getPath());

            return "redirect:/";
        } else {
            mainLogger.info(errors.getAllErrors());
            return "add_url";
        }
    }
}
