package club.dagomys.siteparcer.src.repositories;

import club.dagomys.siteparcer.src.entity.Lemma;
import club.dagomys.siteparcer.src.entity.Site;
import club.dagomys.siteparcer.src.exception.LemmaNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface LemmaRepository extends JpaRepository<Lemma, Integer> {

    Optional<List<Lemma>> findAllLemmaBySite(Site site) throws LemmaNotFoundException;

    Optional<List<Lemma>> findAllByLemma(String lemma) throws LemmaNotFoundException;

    @Transactional
    void deleteLemmaBySite(Site site);


}