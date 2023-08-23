package club.dagomys.siteparcer.src.repos;

import club.dagomys.siteparcer.src.entity.Field;
import club.dagomys.siteparcer.src.entity.FieldSelector;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FieldRepository extends JpaRepository<Field, Integer> {

    Optional<Field> findFieldBySelector(FieldSelector selector);
}
