package club.dagomys.siteparcer.src.controllers;

import club.dagomys.lemmatisator.scr.LemmaCounter;
import club.dagomys.siteparcer.src.entity.FieldSelector;
import club.dagomys.siteparcer.src.entity.MainLog4jLogger;
import club.dagomys.siteparcer.src.entity.Page;
import club.dagomys.siteparcer.src.entity.URLRequest;
import club.dagomys.siteparcer.src.services.FieldService;
import club.dagomys.siteparcer.src.services.LemmaService;
import club.dagomys.siteparcer.src.services.PageService;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;


@Controller
//@RequestMapping("/frontend")
public class MainController implements WebMvcConfigurer {

    private Logger mainLogger = MainLog4jLogger.getInstance();

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

    @GetMapping(value = "/addUrl")
    public String getAddUrlPage(Model model, @ModelAttribute("URL") URLRequest URL) {
        return "add_url";
    }

    @GetMapping(value = {"/{id}"})
    public String getPageById(@ModelAttribute("id") Integer id, Model model) {
        Page findPage = pageService.getPageById(id);
        Map<String, Integer> indexedPage = pageService.indexingAllPages();
        model.addAttribute("findPage", findPage);
//        model.addAttribute("indexedPage", indexedPage);
        model.addAttribute("indexedPage", indexedPage);
        lemmaService.saveAllLemmas();
        return "update_page";
    }

    @PostMapping("/addUrl")
    public String addUrl(@Valid @ModelAttribute("URL") URLRequest URL, Errors errors, Model model) {
        if (!errors.hasErrors()) {
            mainLogger.info(URL);
            pageService.startSiteParse(URL.getPath());
            return "redirect:/";
        } else {
            mainLogger.info(errors.getAllErrors());
            return "add_url";
        }

    }

    private boolean urlChecker(String url) {
        Pattern urlPattern = Pattern.compile("(https?:\\/\\/(?:www\\.|(?!www))[a-zA-Z0-9][a-zA-Z0-9-]+[a-zA-Z0-9]\\.[^\\s]{2,}|www\\.[a-zA-Z0-9][a-zA-Z0-9-]+[a-zA-Z0-9]\\.[^\\s]{2,}|https?:\\/\\/(?:www\\.|(?!www))[a-zA-Z0-9]+\\.[^\\s]{2,}|www\\.[a-zA-Z0-9]+\\.[^\\s]{2,})");
        return
                urlPattern.matcher(url).find();
    }
}
