package club.dagomys.siteparcer.src.controllers;

import club.dagomys.siteparcer.src.entity.Site;
import club.dagomys.siteparcer.src.entity.request.URLRequest;
import club.dagomys.siteparcer.src.services.MainService;
import club.dagomys.siteparcer.src.services.SearchIndexService;
import club.dagomys.siteparcer.src.services.SiteParserRunner;
import club.dagomys.siteparcer.src.services.SiteService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping(value = {"/startIndexing"})
    public String startIndexing(Model model) {
        mainService.startIndexingAllSites();
        return "redirect:/lemmas";
    }

    @GetMapping(value = {"/stopIndexing"})
    public ResponseEntity<Boolean> stopIndexing(){
        return new ResponseEntity<>(HttpStatus.OK);
    }


    @GetMapping(value = "/addUrl")
    public String getAddUrlPage(Model model, @ModelAttribute("URL") URLRequest URL) {
        return "add_url";
    }

    @PostMapping("/addUrl")
    public String addUrl(@Valid @ModelAttribute("URL") URLRequest URL, Errors errors, Model model) {
        if (!errors.hasErrors()) {
            mainLogger.info(URL);
            Site newSite = new Site(URL.getPath(), "");
            siteService.saveSite(newSite);
            return "redirect:/sites";
        } else {
            mainLogger.info(errors.getAllErrors());
            return "add_url";
        }
    }

    @DeleteMapping(value = "/sites/{id}")
    public String deleteSite(@PathVariable("id") int id){
        Site findSite = siteService.getSite(id);
        siteService.deleteSite(findSite);
        return  "redirect:/sites";
    }

    @GetMapping("/sites/{id}")
    public String getSite(@PathVariable(value="id") Integer id) {
        mainService.startIndexingSite(siteService.getSite(id));
        return "redirect:/sites";
    }
}
