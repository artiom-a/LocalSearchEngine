package club.dagomys.siteparcer.src.controllers;

import club.dagomys.siteparcer.src.entity.Lemma;
import club.dagomys.siteparcer.src.entity.SearchIndex;
import club.dagomys.siteparcer.src.entity.Site;
import club.dagomys.siteparcer.src.services.LemmaService;
import club.dagomys.siteparcer.src.services.MainService;
import club.dagomys.siteparcer.src.services.SearchIndexService;
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
    private LemmaService lemmaService;

    @GetMapping("/startIndexing")
    public void startIndexing() {
        searchIndexService.getAllIndexes().forEach(System.out::println);
    }

    @GetMapping("/stopIndexing")
    public void stopIndexing() {
        searchIndexService.getAllIndexes().forEach(System.out::println);
    }

    @GetMapping(value = "/test")
    public ResponseEntity<List<Lemma>> getTestResponse() {
        JsonString s = new JsonString();
        s.setValue("temp");
        return new ResponseEntity<>(lemmaService.getAllLemma(), HttpStatus.OK);
    }


    @GetMapping(value = "/statistics", produces = "application/json")
    public @ResponseBody
    ResponseEntity<JsonString> getStatistic() {
        Lemma lemma = new Lemma("cvbncvb",2);
        JsonString s = new JsonString();
        s.setValue("temp");
        return new ResponseEntity<>(s, HttpStatus.OK);
    }

    public ResponseEntity<SearchIndex> getSearchResults(
            @RequestParam String query,
            @RequestParam(name = "site", required = false, defaultValue = "all") Site site,
            @RequestParam(name = "offset", required = false, defaultValue = "0") int offset,
            @RequestParam(name = "limit", required = false, defaultValue = "0") int limit
    ) {
        
        return new ResponseEntity<>(null, HttpStatus.ACCEPTED);
    }
}
