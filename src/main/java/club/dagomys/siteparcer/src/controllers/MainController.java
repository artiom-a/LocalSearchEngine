package club.dagomys.siteparcer.src.controllers;

import club.dagomys.siteparcer.src.entity.Field;
import club.dagomys.siteparcer.src.repos.PageRepository;
import club.dagomys.siteparcer.src.services.FieldService;
import club.dagomys.siteparcer.src.services.PageService;
import club.dagomys.siteparcer.src.services.SiteParserRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.Banner;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;


@Controller
public class MainController {
    @Autowired
    private PageRepository pageRepository;

    @Autowired
    private PageService pageService;

    @Autowired
    private FieldService fieldService;

    @GetMapping(value = {"/", "/index"})
    public String getMainPage (Model model) {
        fieldService.saveField(new Field("title","title", 1f));
        fieldService.saveField(new Field("body","body", 0.8f));
        return "index";
    }

    @GetMapping(value = {"/addUrl"})
    public String getAddUrlPage (Model model) {
        return "add_url";
    }

    @PostMapping()
    public String addUrl(@ModelAttribute("url") String url){
        new SiteParserRunner(url).run();
        return "redirect:/";
    }
}
