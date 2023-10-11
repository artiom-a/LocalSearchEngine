package club.dagomys.siteparcer.src.controllers;

import club.dagomys.siteparcer.src.dto.Link;
import club.dagomys.siteparcer.src.dto.request.URLRequest;
import club.dagomys.siteparcer.src.entity.Site;
import club.dagomys.siteparcer.src.services.MainService;
import jakarta.validation.Valid;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.ExecutionException;

@Controller
@RequestMapping("/new")
public class IndexingController {
    private final Logger mainLogger = LogManager.getLogger(IndexingController.class);

    @Autowired
    private MainService mainService;


    @GetMapping(value = {"/startIndexing"})
    public String startIndexing(Model model) {
        mainService.startIndexingSites(true, null);
        return "redirect:/new/sites";
    }

    @GetMapping(value = {"/stopIndexing"})
    public String stopIndexing() {
        mainService.stopIndexingSites();
        return "redirect:/new";
    }


    @GetMapping(value = "/addUrl")
    public String getAddUrlPage(Model model, @ModelAttribute("URL") URLRequest URL) {
        return "/frontend/add_url";
    }

    @PostMapping("/new/addUrl")
    public String addUrl(@Valid @ModelAttribute("URL") URLRequest URL, Errors errors, Model model) {
        if (!errors.hasErrors()) {
            mainLogger.info(URL);
            Link rootLink = new Link(URL.getPath());
            Site newSite = new Site(rootLink);
            if (!newSite.getUrl().endsWith("/")) {
                newSite.setUrl(newSite.getUrl().concat("/"));
            }
            mainService.getSiteService().saveAndFlush(newSite);
            return "redirect:/new/sites";
        } else {
            mainLogger.info(errors.getAllErrors());
            return "/new/add_url";
        }
    }

    @DeleteMapping(value = "/sites/{id}")
    public String deleteSite(@PathVariable("id") int id) {
        Site findSite = mainService.getSiteService().findById(id).orElseThrow();
        mainService.getSiteService().delete(findSite);
        return "redirect:/new/sites";
    }

    @GetMapping("/sites/{id}")
    public String getSite(@PathVariable(value = "id") Integer id) throws ExecutionException, InterruptedException {
        Site findSite = mainService.getSiteService().findById(id).orElseThrow();
        mainService.startIndexingSites(false, findSite);
        return "redirect:/new/sites";
    }
}
