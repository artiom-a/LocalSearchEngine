package club.dagomys.siteparcer.src.controllers;

import club.dagomys.siteparcer.src.dto.Link;
import club.dagomys.siteparcer.src.dto.request.URLRequest;
import club.dagomys.siteparcer.src.dto.response.Response;
import club.dagomys.siteparcer.src.entity.Site;
import club.dagomys.siteparcer.src.services.IndexingService;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import jakarta.validation.Valid;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.ExecutionException;

@Controller
public class IndexingController {
    private final Logger mainLogger = LogManager.getLogger(IndexingController.class);

    @Autowired
    private IndexingService indexingService;

    @GetMapping("/api/startIndexing")
    public @ResponseBody ResponseEntity<Response> startIndexing() throws InterruptedException {
        Response response = indexingService.startIndexingSites(true, null);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/api/stopIndexing")
    public @ResponseBody ResponseEntity<Response> stopIndexing() {
        Response response = indexingService.stopIndexingSites();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/api/indexPage")
    public @ResponseBody ResponseEntity<Response> indexPage(@Valid @ModelAttribute("url") URLRequest URL, Errors errors) {
        Response response = indexingService.reindexPage(URL, errors);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping(value = {"/new/startIndexing"})
    public String startIndexing(Model model) {
        indexingService.startIndexingSites(true, null);
        return "redirect:/new/sites";
    }

    @GetMapping(value = "/new/addUrl")
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
            indexingService.getSiteRepository().saveAndFlush(newSite);
            return "redirect:/new/sites";
        } else {
            mainLogger.info(errors.getAllErrors());
            return "/new/add_url";
        }
    }

    @DeleteMapping(value = "/new/sites/{id}")
    public String deleteSite(@PathVariable("id") int id) {
        Site findSite = indexingService.getSiteRepository().findById(id).orElseThrow();
        indexingService.getSiteRepository().delete(findSite);
        return "redirect:/new/sites";
    }

    @GetMapping("/new/sites/{id}")
    public String getSite(@PathVariable(value = "id") Integer id) throws ExecutionException, InterruptedException {
        Site findSite = indexingService.getSiteRepository().findById(id).orElseThrow();
        indexingService.startIndexingSites(false, findSite);
        return "redirect:/new/sites";
    }
}
