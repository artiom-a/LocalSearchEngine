package club.dagomys.siteparcer.src.entity;

import club.dagomys.siteparcer.src.dto.FieldSelector;
import jakarta.persistence.*;
import lombok.*;


@Getter
@Setter
@RequiredArgsConstructor
@ToString
@Entity
@AllArgsConstructor
public class Field {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String name;

    @Enumerated(EnumType.STRING)
    private FieldSelector selector;

    private float weight;

    public Field(String name, FieldSelector selector, float weight) {
        this.name = name;
        this.selector = selector;
        this.weight = weight;
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Field field = (Field) o;

        if (!name.equals(field.name)) return false;
        return selector == field.selector;
    }
}
