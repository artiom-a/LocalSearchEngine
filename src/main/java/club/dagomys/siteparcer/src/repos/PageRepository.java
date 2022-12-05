package club.dagomys.siteparcer.src.repos;

import club.dagomys.siteparcer.src.entity.Page;
import club.dagomys.siteparcer.src.entity.Site;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PageRepository extends JpaRepository<Page, Integer> {

    public Optional<List<Page>> getPageBySite(Site site);
    Optional<Page> findByRelPathAndSite(String path, Site site);
}
