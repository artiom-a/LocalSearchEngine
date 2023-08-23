package club.dagomys.siteparcer.src.repos;

import club.dagomys.siteparcer.src.entity.Lemma;
import club.dagomys.siteparcer.src.entity.Site;
import club.dagomys.siteparcer.src.exception.LemmaNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface LemmaRepository extends JpaRepository<Lemma, Integer> {
    Optional<Lemma> findByLemma(String name) throws LemmaNotFoundException;

    Optional<Lemma> findByLemmaAndSite(String name, Site site) throws LemmaNotFoundException;

    Optional<List<Lemma>> findAllLemmaBySite(Site site) throws LemmaNotFoundException;

    Optional<List<Lemma>> findAllByLemma(String lemma) throws LemmaNotFoundException;

    Optional<Set<Lemma>> getLemmaSetBySite(Site site);

    @Transactional
    void deleteLemmaBySite(Site site);


}