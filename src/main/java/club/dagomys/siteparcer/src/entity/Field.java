package club.dagomys.siteparcer.src.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;

@Data
@ToString
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Field {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private  String name;
    @Enumerated(EnumType.STRING)
    private  FieldSelector selector;
    private  float weight;

    public Field(String name, FieldSelector selector, float weight){
        this.name = name;
        this.selector = selector;
        this.weight = weight;
    }

}
