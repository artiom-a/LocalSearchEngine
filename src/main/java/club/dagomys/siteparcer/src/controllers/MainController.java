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
        model.addAttribute("sites", siteService.getAllSites());
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
        return new ResponseEntity<String>("Тест", HttpStatus.OK);
    }

    @GetMapping(value = {"/{id}"})
    public String getPageById(@ModelAttribute("id") Integer id, Model model) {
        model.addAttribute("findPage", pageService.getPageById(id));
        return "update_page";
    }

    @GetMapping(value = {"/lemmas"})
    public String getAllLemma(Model model) {
        model.addAttribute("lemmas", lemmaService.getAllLemma());
        return "/frontend/lemmas";
    }

    @GetMapping(value = {"/sites"})
    public String getAllSites(Model model) {
        model.addAttribute("sites", siteService.getAllSites());
        return "/frontend/sites";
    }

    @GetMapping(value = {"/deleteAllLemma"})
    public String deleteAllLemma(Model model) {
        lemmaService.deleteAllLemma();
        return "redirect:/frontend/lemmas";
    }
}
