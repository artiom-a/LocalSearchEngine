package club.dagomys.siteparcer.src.controllers;

import club.dagomys.siteparcer.src.entity.request.SearchRequest;
import club.dagomys.siteparcer.src.entity.request.URLRequest;
import club.dagomys.siteparcer.src.entity.response.DashboardResponse;
import club.dagomys.siteparcer.src.entity.response.Response;
import club.dagomys.siteparcer.src.entity.response.SearchResponse;

import club.dagomys.siteparcer.src.services.MainService;
import club.dagomys.siteparcer.src.services.SearchService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Objects;

@RestController
@RequestMapping("/api")
public class APIController {
    private static final Logger mainLogger = LogManager.getLogger(APIController.class);
    @Autowired
    private MainService mainService;

    @Autowired
    private SearchService searchService;

    @GetMapping("/startIndexing")
    public ResponseEntity<Response> startIndexing() throws InterruptedException {
        Response response = new Response();
        response.setResult(mainService.startIndexingSites(true, null));
        if (mainService.getIsIndexing()) {
            response.setError("Индексация уже запущена");
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/stopIndexing")
    public ResponseEntity<Response> stopIndexing() {
        Response response = new Response();
        response.setResult(mainService.stopIndexingSites());
        if (!mainService.getIsIndexing()) {
            response.setError("Индексация не запущена");
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    @GetMapping(value = "/statistics", produces = "application/json")
    public @ResponseBody
    ResponseEntity<DashboardResponse> getStatistic() {
        DashboardResponse statistics = mainService.getStatistic();
        return new ResponseEntity<>(statistics, HttpStatus.OK);
    }

    @GetMapping(value = "/search")
    public ResponseEntity<SearchResponse> getSearchResults(
            @RequestParam(name = "query") SearchRequest query,
            @RequestParam(name = "site", required = false, defaultValue = "all") String site
    ) {
        SearchResponse response = searchService.search(query, site);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/indexPage")
    public ResponseEntity<Response> indexPage(@Valid @ModelAttribute("url") URLRequest URL, Errors errors, Model model) {
        Response response = new Response();
        if (!errors.hasErrors()) {
            response.setResult(mainService.getPageService().reindexPage(URL));
            if (!response.getResult()) {
                response.setError("Данная страница находится за пределами сайтов указанных в конфигурационном файле");
            }
        } else {
            response.setError(Objects.requireNonNull(errors.getFieldError()).getDefaultMessage());
            mainLogger.error(errors.getAllErrors());
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
