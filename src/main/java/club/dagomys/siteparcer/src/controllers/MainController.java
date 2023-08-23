package club.dagomys.siteparcer.src.controllers;

import club.dagomys.siteparcer.src.services.MainService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Controller
public class MainController implements WebMvcConfigurer {

    private final Logger mainLogger = LogManager.getLogger(MainController.class);

    @Autowired
    private MainService mainService;


    @GetMapping(value = {"/", "/index"})
    public String getMainPage(Model model) {
        return "index";
    }

    @GetMapping(value = {"/new"})
    public String getOldPage(Model model) {
        model.addAttribute("sites", mainService.getSiteService().count());
        model.addAttribute("pages", mainService.getPageService().count());
        return "/frontend/index";
    }

    @GetMapping(value = {"/new/{id}"})
    public String getPageById(@ModelAttribute("id") Integer id, Model model) {
        model.addAttribute("findPage", mainService.getPageService().findById(id));
        return "/frontend/update_page";
    }

    @GetMapping(value = {"/new/lemmas"})
    public String getAllLemma(Model model) {
        model.addAttribute("lemmas", mainService.getLemmaService().findAll());
        return "/frontend/lemmas";
    }

    @GetMapping(value = {"/new/sites"})
    public String getAllSites(Model model) {
        model.addAttribute("sites", mainService.getSiteService().findAll());
        return "/frontend/sites";
    }

    @GetMapping(value = {"/new/deleteAllLemma"})
    public String deleteAllLemma(Model model) {
        mainService.getLemmaService().deleteAll();
        return "redirect:/frontend/lemmas";
    }
}
