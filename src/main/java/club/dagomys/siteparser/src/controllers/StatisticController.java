package club.dagomys.siteparser.src.controllers;

import club.dagomys.siteparser.src.dto.response.DashboardResponse;
import club.dagomys.siteparser.src.services.StatisticService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Controller
@Slf4j
public class StatisticController implements WebMvcConfigurer {
    @Autowired
    private StatisticService statisticService;

    @GetMapping(value = "/api/statistics", produces = "application/json")
    public @ResponseBody
    ResponseEntity<DashboardResponse> getStatistic() {
        DashboardResponse statistics = statisticService.getStatistic();
        return new ResponseEntity<>(statistics, HttpStatus.OK);
    }

    @GetMapping(value = {"/", "/index"})
    public String getMainPage() {
        return "index";
    }

    @GetMapping(value = {"/new"})
    public String getOldPage(Model model) {
        model.addAttribute("sites", statisticService.getSiteRepository().findAll());
        model.addAttribute("pages", statisticService.getPageRepository().findAll());
        return "/frontend/index";
    }

    @GetMapping(value = {"/new/{id}"})
    public String getPageById(@ModelAttribute("id") Integer id, Model model) {
        model.addAttribute("findPage", statisticService.getPageRepository().findById(id));
        return "/frontend/update_page";
    }

    @GetMapping(value = {"/new/lemmas"})
    public String getAllLemma(Model model) {
        model.addAttribute("lemmas", statisticService.getLemmaRepository().findAll());
        return "/frontend/lemmas";
    }

    @GetMapping(value = {"/new/sites"})
    public String getAllSites(Model model) {
        model.addAttribute("sites", statisticService.getSiteRepository().findAll());
        return "/frontend/sites";
    }

    @GetMapping(value = {"/new/deleteAllLemma"})
    public String deleteAllLemma(Model model) {
        statisticService.getLemmaRepository().deleteAll();
        return "redirect:/frontend/lemmas";
    }

}
