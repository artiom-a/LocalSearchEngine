package club.dagomys.siteparcer.src.controllers;

import club.dagomys.siteparcer.src.dto.request.SearchRequest;
import club.dagomys.siteparcer.src.dto.response.SearchResponse;
import club.dagomys.siteparcer.src.exception.SearchEngineException;
import club.dagomys.siteparcer.src.services.SearchService;
import jakarta.validation.Valid;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;



@Controller
@RequestMapping("/new")
public class SearchController {
    private final Logger mainLogger = LogManager.getLogger(SearchController.class);
    private SearchResponse searchResponse = new SearchResponse();


    @Autowired
    private SearchService searchService;

    @GetMapping("/search")
    public String getSearchPage(Model model, @ModelAttribute("searchRequest") SearchRequest searchRequest) {
        model.addAttribute("indexList", searchResponse);
        return "/frontend/search";
    }

    @PostMapping("/search")
    public String search(@Valid @ModelAttribute("searchRequest") SearchRequest searchRequest, Errors errors, Model model,
                         @RequestParam(name = "site", required = false, defaultValue = "all") String site,
                         @RequestParam(value = "offset", required = false, defaultValue = "0") Integer offset,
                         @RequestParam(value = "limit", required = false, defaultValue = "20") Integer limit) throws SearchEngineException {
        if (!errors.hasErrors()) {
            searchResponse = searchService.search(searchRequest, site, offset, limit, errors );
            return "redirect:/new/search";
        } else {
            mainLogger.info(errors.getAllErrors());
            return "/new/search";
        }

    }
}
