package club.dagomys.siteparcer.src.services;

import club.dagomys.siteparcer.src.entity.SearchIndex;
import club.dagomys.siteparcer.src.repos.SearchIndexRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SearchIndexService {

    @Autowired
    private SearchIndexRepository searchIndexRepository;

    public SearchIndex saveIndex(SearchIndex searchIndex) {
        return searchIndexRepository.save(searchIndex);
    }

}
