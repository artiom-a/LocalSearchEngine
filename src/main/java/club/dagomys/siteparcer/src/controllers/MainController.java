package club.dagomys.siteparcer.src.controllers;

import club.dagomys.siteparcer.src.entity.*;
import club.dagomys.siteparcer.src.services.FieldService;
import club.dagomys.siteparcer.src.services.LemmaService;
import club.dagomys.siteparcer.src.services.PageService;
import club.dagomys.siteparcer.src.services.SearchIndexService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.validation.Valid;
import java.util.Map;


@Controller
//@RequestMapping("/frontend")
public class MainController implements WebMvcConfigurer {

    private Logger mainLogger = LogManager.getLogger(MainController.class);

    @Autowired
    private PageService pageService;

    @Autowired
    private FieldService fieldService;

    @Autowired
    private LemmaService lemmaService;

    @Autowired
    private SearchIndexService searchIndexService;

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
    public ResponseEntity<Lemma> startCrawler(Model model) {
        Map<String, Integer> indexedPages = lemmaService.lemmaFrequencyCounter();
        model.addAttribute("indexedPage", indexedPages);
        return new ResponseEntity<>(HttpStatus.OK);
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

}
