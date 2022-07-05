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
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    private  String name;
    private  String selector;
    private  float weight;

    public Field(String name, String selector, float weight){
        this.name = name;
        this.selector = selector;
        this.weight = weight;
    }

}
