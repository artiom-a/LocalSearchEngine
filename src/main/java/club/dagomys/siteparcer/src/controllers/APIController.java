package club.dagomys.siteparcer.src.controllers;

import club.dagomys.siteparcer.src.services.MainService;
import club.dagomys.siteparcer.src.services.SearchIndexService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.Mapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class APIController {
    @Autowired
    private SearchIndexService searchIndexService;

    @Autowired
    private MainService mainService;

    @GetMapping("/api")
    public void startIndexing(){
        searchIndexService.getAllIndexes().forEach(System.out::println);
    }

}
