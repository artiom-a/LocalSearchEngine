package club.dagomys.siteparcer.src.controllers;

import club.dagomys.siteparcer.src.entity.Field;
import club.dagomys.siteparcer.src.entity.MainLog4jLogger;
import club.dagomys.siteparcer.src.repos.PageRepository;
import club.dagomys.siteparcer.src.services.FieldService;
import club.dagomys.siteparcer.src.services.PageService;
import club.dagomys.siteparcer.src.services.SiteParserRunner;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.Banner;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;


@Controller
//@RequestMapping("/frontend")
public class MainController {
    private Logger mainLogger = MainLog4jLogger.getIstance();

    @Autowired
    private PageService pageService;

    @GetMapping(value = {"/", "/index"})
    public String getMainPage (Model model) {
        model.addAttribute("pages", pageService.getAllPages());
        return "index";
    }

    @GetMapping(value = {"/addUrl"})
    public String getAddUrlPage (Model model) {
        return "add_url";
    }

    @PostMapping()
    public String addUrl(@Valid @RequestParam String url, Model model){
        if (urlChecker(url)) {
            pageService.startSiteParse(url);
        } else {
            mainLogger.info("Введите корректный URL");
        }
        return "redirect:/";
    }

    private boolean urlChecker(String url) {
        Pattern urlPattern = Pattern.compile("(https?:\\/\\/(?:www\\.|(?!www))[a-zA-Z0-9][a-zA-Z0-9-]+[a-zA-Z0-9]\\.[^\\s]{2,}|www\\.[a-zA-Z0-9][a-zA-Z0-9-]+[a-zA-Z0-9]\\.[^\\s]{2,}|https?:\\/\\/(?:www\\.|(?!www))[a-zA-Z0-9]+\\.[^\\s]{2,}|www\\.[a-zA-Z0-9]+\\.[^\\s]{2,})");
        return
                urlPattern.matcher(url).find();
    }
}
