package club.dagomys.siteparcer.src.controllers;

import club.dagomys.siteparcer.src.entity.*;
import club.dagomys.siteparcer.src.entity.request.SearchRequest;
import club.dagomys.siteparcer.src.entity.request.SearchResponse;
import club.dagomys.siteparcer.src.entity.request.URLRequest;
import club.dagomys.siteparcer.src.services.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.validation.Valid;
import java.util.*;


@Controller
//@RequestMapping("/frontend")
public class MainController implements WebMvcConfigurer {

    private Logger mainLogger = LogManager.getLogger(MainController.class);
    private List<Page> findIndexList = new ArrayList<>();
    private List<SearchResponse> searchResponses = new ArrayList<>();

    @Autowired
    private PageService pageService;

    @Autowired
    private FieldService fieldService;

    @Autowired
    private LemmaService lemmaService;

    @Autowired
    private SearchIndexService searchIndexService;

    @Autowired
    private SearchService searchService;

    @GetMapping(value = {"/", "/index"})
    public String getMainPage(Model model) {
        model.addAttribute("pages", pageService.getAllPages());
        return "index";
    }

    @GetMapping(value = {"/new"})
    public String getOldPage(Model model) {
        model.addAttribute("pages", pageService.getAllPages());
        return "/frontend/index";
    }

    @GetMapping("/search")
    public String getSearchPage(Model model, @ModelAttribute("searchRequest") SearchRequest searchRequest) {
        model.addAttribute("indexList", searchResponses);
        return "search";
    }

    @GetMapping(value = "/addUrl")
    public String getAddUrlPage(Model model, @ModelAttribute("URL") URLRequest URL) {
        return "add_url";
    }

    @GetMapping(value = {"/{id}"})
    public String getPageById(@ModelAttribute("id") Integer id, Model model) {
        Page findPage = pageService.getPageById(id);
        Map<String, Lemma> lemmas = lemmaService.startCountingLemmasOnPage(findPage);
        model.addAttribute("lemmas", lemmas);
        model.addAttribute("findPage", findPage);
        return "update_page";
    }

    @GetMapping(value = {"/lemmas"})
    public String getAllLemma(Model model) {
        model.addAttribute("lemmas", lemmaService.gelAllLemma());
        return "lemmas";
    }

    @GetMapping(value = {"/deleteAllLemma"})
    public String deleteAllLemma(Model model) {
        lemmaService.deleteAllLemma();
        return "redirect:/lemmas";
    }

    @GetMapping(value = {"/startCrawler"})
    public String startCrawler(Model model) {
        searchIndexService.startIndexingAllPages();
        return "redirect:/lemmas";
    }

    @PostMapping("/addUrl")
    public String addUrl(@Valid @ModelAttribute("URL") URLRequest URL, Errors errors, Model model) {
        if (!errors.hasErrors()) {
            mainLogger.info(URL);
//            siteService.saveSite(new Site(URL.getPath(),"NTCN"));
            pageService.startSiteParse(URL.getPath());
            return "redirect:/";
        } else {
            mainLogger.info(errors.getAllErrors());
            return "add_url";
        }

    }

    @GetMapping("/statistics")
    public ResponseEntity<Object> getStatistics(){
        return ResponseEntity.ok (searchResponses);
    }

    @PostMapping("/search")
    public String search(@Valid @ModelAttribute("searchRequest") SearchRequest searchRequest, Errors errors, Model model) {
        if (!errors.hasErrors()) {
            searchResponses = searchService.search(searchRequest);
            return "redirect:/search";
        } else {
            mainLogger.info(errors.getAllErrors());
            return "search";
        }

    }

}
