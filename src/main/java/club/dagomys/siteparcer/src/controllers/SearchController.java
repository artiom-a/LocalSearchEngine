package club.dagomys.siteparcer.src.controllers;

import club.dagomys.siteparcer.src.entity.request.SearchRequest;
import club.dagomys.siteparcer.src.entity.response.SearchResponse;
import club.dagomys.siteparcer.src.services.SearchService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@Controller
public class SearchController {
    private Logger mainLogger = LogManager.getLogger(SearchController.class);
    private List<SearchResponse> searchResponses = new ArrayList<>();


    @Autowired
    private SearchService searchService;

    @GetMapping("/search")
    public String getSearchPage(Model model, @ModelAttribute("searchRequest") SearchRequest searchRequest) {
        model.addAttribute("indexList", searchResponses);
        return "search";
    }

    @GetMapping("/statistics")
    public ResponseEntity<Object> getStatistics(){
        return ResponseEntity.ok (searchResponses);
    }

    @PostMapping("/search")
    public String search(@Valid @ModelAttribute("searchRequest") SearchRequest searchRequest, Errors errors, Model model) {
        if (!errors.hasErrors()) {
            searchResponses = searchService.search(searchRequest).join();
            return "redirect:/search";
        } else {
            mainLogger.info(errors.getAllErrors());
            return "search";
        }

    }
}
