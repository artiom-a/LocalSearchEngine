package club.dagomys.siteparser.src.repositories;

import club.dagomys.siteparser.src.entity.Page;
import club.dagomys.siteparser.src.entity.Site;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PageRepository extends JpaRepository<Page, Integer> {
    Optional<List<Page>> findAllPageBySite(Site site);

    Optional<Page> findByRelPathAndSite(String path, Site site);

}
