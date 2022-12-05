package club.dagomys.siteparcer.src.controllers;

import club.dagomys.siteparcer.src.entity.*;
import club.dagomys.siteparcer.src.services.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.*;
import java.util.concurrent.ExecutionException;


@Controller
//@RequestMapping("/frontend")
public class MainController implements WebMvcConfigurer {

    private Logger mainLogger = LogManager.getLogger(MainController.class);
    private List<Page> findIndexList = new ArrayList<>();
@Autowired
private MainService mainService;

    @Autowired
    private PageService pageService;

    @Autowired
    private FieldService fieldService;

    @Autowired
    private LemmaService lemmaService;

    @Autowired
    private SiteService siteService;



    @GetMapping(value = {"/", "/index"})
    public String getMainPage(Model model) {
        model.addAttribute("pages", pageService.getAllPages());
        return "index";
    }

    @GetMapping(value = {"/new"})
    public String getOldPage(Model model) {
        return "/frontend/index";
    }




    @GetMapping(value = {"/test"})
    public ResponseEntity<String> testMethod(Model model)
    {
//        Page page1 = new Page("https://test.ru");
//        Page page2 = new Page("https://test1.ru");

//        pageService.savePage(page1);
//        pageService.savePage(page2);

//        Page page3 = new Page("https://test1.ru");
//        page3.setContent("jkjkjlkjl");
//        page3.setStatusCode(404);
//        pageService.updatePage(page3);
//        Lemma lemma = new Lemma("тест", 1);
//        Lemma lemma1 = new Lemma("тест1", 2);
//        Lemma lemma2 = new Lemma("тсет3", 3);
//
//        Page page = new Page("page1");
//        Page page1 = new Page("page2");
//        Page page2 = new Page("page3");
//
//        Site site = new Site("https://site.ru","site" );
//        Site site1 = new Site("https://site1.ru","site1" );
//
//
//
//
//        mainService.getLemmaService().saveLemma(lemma);
//        mainService.getLemmaService().saveLemma(lemma1);
//        mainService.getLemmaService().saveLemma(lemma2);
//        mainService.getPageService().savePage(page);
//        mainService.getPageService().savePage(page1);
//        mainService.getPageService().savePage(page2);
//        try {
//            Thread.sleep(1000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        SearchIndex searchIndex = new SearchIndex(page, lemmaService.findLemma("тест").get(), 2);
//        SearchIndex searchIndex1 = new SearchIndex(page1, lemmaService.findLemma("тест1").get(), 3);
//        SearchIndex searchIndex2 = new SearchIndex(page2, lemmaService.findLemma("тест1").get(), 1);
//        SearchIndex searchIndex3 = new SearchIndex(page1, lemmaService.findLemma("тсет3").get(), 1);
//        mainService.getSearchIndexService().saveIndex(searchIndex);
//        mainService.getSearchIndexService().saveIndex(searchIndex1);
//        mainService.getSearchIndexService().saveIndex(searchIndex2);
//        mainService.getSearchIndexService().saveIndex(searchIndex3);
        return new ResponseEntity<String>("Тест", HttpStatus.OK);
    }

    @GetMapping(value = {"/{id}"})
    public String getPageById(@ModelAttribute("id") Integer id, Model model) {
        Page findPage = pageService.getPageById(id);
//        Map<String, Lemma> lemmas = mainService.countLemmasOnPage(findPage);
//        model.addAttribute("lemmas", lemmas);
        model.addAttribute("findPage", findPage);
        return "update_page";
    }

    @GetMapping(value = {"/lemmas"})
    public String getAllLemma(Model model) {
        model.addAttribute("lemmas", lemmaService.getAllLemma());
        return "lemmas";
    }

    @GetMapping(value = {"/sites"})
    public String getAllSites(Model model) {
        model.addAttribute("sites", siteService.getAllSites());
        return "sites";
    }

    @GetMapping(value = {"/deleteAllLemma"})
    public String deleteAllLemma(Model model) {
        lemmaService.deleteAllLemma();
        return "redirect:/lemmas";
    }
}
