package club.dagomys.siteparcer.src.repos;

import club.dagomys.siteparcer.src.entity.Field;
import club.dagomys.siteparcer.src.entity.Page;
import club.dagomys.siteparcer.src.entity.Site;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SiteRepository extends CrudRepository<Site, Integer> {

    Optional<Site> findByUrl(String url);
}