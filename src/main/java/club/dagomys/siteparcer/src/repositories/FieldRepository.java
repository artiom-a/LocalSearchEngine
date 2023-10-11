package club.dagomys.siteparcer.src.repositories;

import club.dagomys.siteparcer.src.dto.FieldSelector;
import club.dagomys.siteparcer.src.entity.Field;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FieldRepository extends JpaRepository<Field, Integer> {

    Optional<Field> findFieldBySelector(FieldSelector selector);
}
