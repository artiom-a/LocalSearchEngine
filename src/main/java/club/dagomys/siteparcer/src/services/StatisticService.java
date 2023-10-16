package club.dagomys.siteparcer.src.services;

import club.dagomys.siteparcer.src.dto.response.DashboardResponse;
import club.dagomys.siteparcer.src.dto.response.Detail;
import club.dagomys.siteparcer.src.dto.response.Statistic;
import club.dagomys.siteparcer.src.dto.response.Total;
import club.dagomys.siteparcer.src.entity.Site;
import club.dagomys.siteparcer.src.exception.LemmaNotFoundException;
import club.dagomys.siteparcer.src.repositories.LemmaRepository;
import club.dagomys.siteparcer.src.repositories.PageRepository;
import club.dagomys.siteparcer.src.repositories.SiteRepository;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class StatisticService {

    private final Logger mainLogger = LogManager.getLogger(StatisticService.class);

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
                mainLogger.error(e.getMessage());
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
