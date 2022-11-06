package club.dagomys.siteparcer.src.repos;

import club.dagomys.siteparcer.src.entity.Field;
import club.dagomys.siteparcer.src.entity.Site;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SiteRepository extends CrudRepository<Site, Integer> {

    public Site findByUrl(String url);
}
