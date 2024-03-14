package club.dagomys.siteparser.src.repositories;

import club.dagomys.siteparser.src.dto.FieldSelector;
import club.dagomys.siteparser.src.entity.Field;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FieldRepository extends JpaRepository<Field, Integer> {

    Optional<Field> findFieldBySelector(FieldSelector selector);
}
