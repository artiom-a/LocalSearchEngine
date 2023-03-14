package club.dagomys.siteparcer.src.controllers;

import club.dagomys.siteparcer.src.entity.Lemma;
import club.dagomys.siteparcer.src.entity.SearchIndex;
import club.dagomys.siteparcer.src.entity.Site;
import club.dagomys.siteparcer.src.entity.request.SearchRequest;
import club.dagomys.siteparcer.src.entity.response.DashboardResponse;
import club.dagomys.siteparcer.src.entity.response.SearchResponse;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;

import club.dagomys.siteparcer.src.entity.response.Statistic;
import club.dagomys.siteparcer.src.services.LemmaService;
import club.dagomys.siteparcer.src.services.MainService;
import club.dagomys.siteparcer.src.services.SearchIndexService;
import club.dagomys.siteparcer.src.services.SearchService;
import com.mysql.cj.xdevapi.JsonString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class APIController {
    @Autowired
    private SearchIndexService searchIndexService;

    @Autowired
    private MainService mainService;

    @Autowired
    private SearchService searchService;

    @GetMapping("/startIndexing")
    public void startIndexing() throws InterruptedException {
        mainService.startIndexingSites(true, null);
    }

    @GetMapping("/stopIndexing")
    public void stopIndexing() {
        mainService.stopIndexingSites();
    }


    @GetMapping(value = "/statistics", produces = "application/json")
    public @ResponseBody
    ResponseEntity<DashboardResponse> getStatistic() {
        DashboardResponse statistics = mainService.getStatistic();
        return new ResponseEntity<>(statistics, HttpStatus.OK);
    }

    @GetMapping(value = "/search", produces = "application/json")
    public @ResponseBody ResponseEntity<?> getSearchResults(
            @RequestParam(name = "query", required = false) String query,
            @RequestParam(name = "site", required = false, defaultValue = "all") Site site,
            @RequestParam(name = "offset", required = false, defaultValue = "0") int offset,
            @RequestParam(name = "limit", required = false, defaultValue = "0") int limit
    ) {
        if (query == null) {
            JSONObject response = new JSONObject();

            try {
                response.put("result", false);
                response.put("error", "Задан пустой поисковый запрос");
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }

            return new ResponseEntity<>(response, HttpStatus.OK);
        }
        SearchResponse response = searchService.search(new SearchRequest(query));
        return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
    }
}
