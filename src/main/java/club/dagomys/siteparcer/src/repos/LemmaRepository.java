package club.dagomys.siteparcer.src.repos;

import club.dagomys.siteparcer.src.entity.Lemma;
import club.dagomys.siteparcer.src.entity.Site;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface LemmaRepository extends JpaRepository<Lemma, Integer> {
    Optional<Lemma> findByLemma (String name);
    @Transactional
    Optional<Lemma> findByLemmaAndSite (String name, Site site);
    @Transactional
    void deleteAllBySite(Site site);


}