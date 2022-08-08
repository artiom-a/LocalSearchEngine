package club.dagomys.siteparcer.src.repos;

import club.dagomys.siteparcer.src.entity.Lemma;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LemmaRepository extends JpaRepository<Lemma, Integer> {
    List<Lemma> findByLemma (String name);
}