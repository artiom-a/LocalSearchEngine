package club.dagomys.siteparcer.src.repos;

import club.dagomys.siteparcer.src.entity.Field;
import lombok.experimental.FieldDefaults;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import javax.persistence.criteria.CriteriaBuilder;

@Repository
public interface FieldRepository extends CrudRepository<Field, Integer> {
}
