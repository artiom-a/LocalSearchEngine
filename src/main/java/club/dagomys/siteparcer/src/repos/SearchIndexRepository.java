package club.dagomys.siteparcer.src.repos;

import club.dagomys.siteparcer.src.entity.Lemma;
import club.dagomys.siteparcer.src.entity.Page;
import club.dagomys.siteparcer.src.entity.SearchIndex;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SearchIndexRepository extends JpaRepository<SearchIndex, Integer> {
    List<SearchIndex> findByLemmaOrderByRankDesc(Lemma lemma);
    List<SearchIndex> findByPageOrderByRankDesc(Page page);
    Optional<SearchIndex> findByPageAndLemma(Page page, Lemma lemma);
    List<SearchIndex> findByPage(Page page);
    @Async
    public <S extends SearchIndex> S save(S entity);
}
