package club.dagomys.siteparcer.src.controllers;

import club.dagomys.siteparcer.src.entity.request.SearchRequest;
import club.dagomys.siteparcer.src.entity.response.SearchResponse;
import club.dagomys.siteparcer.src.services.SearchService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Controller
@RequestMapping("/new")
public class SearchController {
    private final Logger mainLogger = LogManager.getLogger(SearchController.class);
    private SearchResponse searchResponse = new SearchResponse();


    @Autowired
    private SearchService searchService;

    @GetMapping("/new/search")
    public String getSearchPage(Model model, @ModelAttribute("searchRequest") SearchRequest searchRequest) {
        model.addAttribute("indexList", searchResponse);
        return "/frontend/search";
    }

    @PostMapping("/search")
    public String search(@Valid @ModelAttribute("searchRequest") SearchRequest searchRequest, Errors errors, Model model,
                         @RequestParam(name = "site", required = false, defaultValue = "all") String site)
    {
        if (!errors.hasErrors()) {
            searchResponse = searchService.search(searchRequest, site);
            return "redirect:/new/search";
        } else {
            mainLogger.info(errors.getAllErrors());
            return "/new/search";
        }

    }
}
