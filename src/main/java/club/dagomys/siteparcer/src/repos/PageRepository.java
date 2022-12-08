package club.dagomys.siteparcer.src.repos;

import club.dagomys.siteparcer.src.entity.Page;
import club.dagomys.siteparcer.src.entity.Site;
import club.dagomys.siteparcer.src.entity.id.PageId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface PageRepository extends JpaRepository<Page, Integer> {

    @Transactional
    Optional<List<Page>> getPageBySite(Site site);
    @Transactional
    Optional<Page> findByRelPathAndSite(String path, Site site);
    boolean existsPageByRelPathAndSite(String path, Site site);
}
