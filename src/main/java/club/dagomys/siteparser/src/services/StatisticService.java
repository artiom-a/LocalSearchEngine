package club.dagomys.siteparser.src.services;

import club.dagomys.siteparser.src.dto.response.DashboardResponse;
import club.dagomys.siteparser.src.dto.response.Detail;
import club.dagomys.siteparser.src.dto.response.Statistic;
import club.dagomys.siteparser.src.dto.response.Total;
import club.dagomys.siteparser.src.entity.Site;
import club.dagomys.siteparser.src.exception.LemmaNotFoundException;
import club.dagomys.siteparser.src.repositories.LemmaRepository;
import club.dagomys.siteparser.src.repositories.PageRepository;
import club.dagomys.siteparser.src.repositories.SiteRepository;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
public class StatisticService {

    @Autowired
    @Getter
    private SiteRepository siteRepository;
    @Autowired
    @Getter
    private PageRepository pageRepository;
    @Autowired
    @Getter
    private LemmaRepository lemmaRepository;

    @Autowired
    private IndexingService indexingService;

    public DashboardResponse getStatistic() {
        DashboardResponse response = new DashboardResponse();
        Statistic statistic = new Statistic();
        ArrayList<Detail> details = new ArrayList<>();
        Total total = new Total();

        AtomicInteger allLemmas = new AtomicInteger();
        AtomicInteger allPages = new AtomicInteger();

        List<Site> siteList = siteRepository.findAll();

        if (siteList.isEmpty()) {
            return new DashboardResponse();
        }

        siteList.forEach(site -> {
            try {
                int pages = pageRepository.findAllPageBySite(site).get().size();
                int lemmas = lemmaRepository.findAllLemmaBySite(site).get().size();
                allPages.updateAndGet(v -> v + pages);
                allLemmas.updateAndGet(v -> v + lemmas);
                details.add(new Detail(site.getUrl(), site.getName(), site.getStatus(), site.getStatusTime(), site.getLastError(), pages, lemmas));
            } catch (LemmaNotFoundException e) {
                log.error(e.getMessage());
            }
        });
        total.setLemmaCount(allLemmas.get());
        total.setPageCount(allPages.get());
        total.setSiteCount((int) siteRepository.count());
        total.setIndexing(indexingService.getIsIndexing().get());
        statistic.setTotal(total);
        statistic.setSiteList(details);
        response.setResult(true);
        response.setStatistics(statistic);

        return response;
    }
}
