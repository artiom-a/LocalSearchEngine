package club.dagomys.siteparcer.src.repos;

import club.dagomys.siteparcer.src.entity.Lemma;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LemmaRepository extends JpaRepository<Lemma, Integer> {
    Lemma findByLemma(String lemma);
}