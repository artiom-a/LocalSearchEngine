package club.dagomys.siteparcer.src.services;

import club.dagomys.siteparcer.src.entity.MainLog4jLogger;
import club.dagomys.siteparcer.src.entity.Page;
import club.dagomys.siteparcer.src.repos.PageRepository;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PageService {
    private Logger mainLogger = MainLog4jLogger.getIstance();

    @Autowired
    PageRepository pageRepository;

    public Page savePage(Page page){
        return pageRepository.save(page);
    }

    public void printLink(Page page){
        mainLogger.info(page);
    }

    public SiteParserRunner siteParse(String url){
        return new SiteParserRunner(url);
    }

}
