package club.dagomys.siteparcer.src.controllers;

import club.dagomys.siteparcer.src.dto.request.SearchRequest;
import club.dagomys.siteparcer.src.dto.request.URLRequest;
import club.dagomys.siteparcer.src.dto.response.DashboardResponse;
import club.dagomys.siteparcer.src.dto.response.Response;
import club.dagomys.siteparcer.src.dto.response.SearchResponse;
import club.dagomys.siteparcer.src.exception.SearchEngineException;
import club.dagomys.siteparcer.src.services.MainService;
import club.dagomys.siteparcer.src.services.SearchService;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import jakarta.validation.Valid;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api")
@OpenAPIDefinition
public class APIController {
    private static final Logger mainLogger = LogManager.getLogger(APIController.class);
    @Autowired
    private MainService mainService;

    @Autowired
    private SearchService searchService;

    @GetMapping("/startIndexing")
    public ResponseEntity<Response> startIndexing() throws InterruptedException {
        Response response = mainService.startIndexingSites(true, null);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/stopIndexing")
    public ResponseEntity<Response> stopIndexing() {
        Response response = mainService.stopIndexingSites();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    @GetMapping(value = "/statistics", produces = "application/json")
    public @ResponseBody
    ResponseEntity<DashboardResponse> getStatistic() {
        DashboardResponse statistics = mainService.getStatistic();
        return new ResponseEntity<>(statistics, HttpStatus.OK);
    }

    @GetMapping("/search")
    public ResponseEntity<SearchResponse> getSearchResults(
            @RequestBody @ModelAttribute("query") @Valid SearchRequest searchRequestQuery, Errors errors,
            @RequestParam(name = "site", required = false, defaultValue = "all") String site,
            @RequestParam(name = "offset", required = false, defaultValue = "0") Integer offset,
            @RequestParam(name = "limit", required = false, defaultValue = "20") Integer limit
    ) throws SearchEngineException {
        SearchResponse response = searchService.search(searchRequestQuery, site, offset, limit, errors);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/indexPage")
    public ResponseEntity<Response> indexPage(@Valid @ModelAttribute("url") URLRequest URL, Errors errors) {
        Response response = mainService.reindexPage(URL, errors);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
