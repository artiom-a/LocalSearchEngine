package club.dagomys.siteparcer.src.controllers;

import club.dagomys.siteparcer.src.entity.*;
import club.dagomys.siteparcer.src.entity.request.URLRequest;
import club.dagomys.siteparcer.src.services.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
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


    @GetMapping(value = "/addUrl")
    public String getAddUrlPage(Model model, @ModelAttribute("URL") URLRequest URL) {
        return "add_url";
    }

    @GetMapping(value = {"/{id}"})
    public String getPageById(@ModelAttribute("id") Integer id, Model model) {
        Page findPage = pageService.getPageById(id);
        Map<String, Lemma> lemmas = mainService.countLemmasOnPage(findPage);
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
}
