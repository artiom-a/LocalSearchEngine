package club.dagomys.siteparcer.src.repos;

import club.dagomys.siteparcer.src.entity.Page;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PageRepository extends CrudRepository<Page, Integer> {
}
