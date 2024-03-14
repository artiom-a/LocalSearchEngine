package club.dagomys.siteparser.src.repositories;

import club.dagomys.siteparser.src.entity.Lemma;
import club.dagomys.siteparser.src.entity.Page;
import club.dagomys.siteparser.src.entity.SearchIndex;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SearchIndexRepository extends JpaRepository<SearchIndex, Integer> {
    List<SearchIndex> findByLemmaOrderByRankDesc(Lemma lemma);

    Optional<SearchIndex> findByPageAndLemma(Page page, Lemma lemma);

    void deleteByPage(Page page);

}
