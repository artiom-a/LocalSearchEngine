package club.dagomys.siteparcer.src.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.Type;

import javax.persistence.*;

@Data
@ToString
@Entity
public class Filed {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    private final String name;
    private final String selector;
    private final float weight;

    public Filed(String name, String selector, float weight){
        this.name = name;
        this.selector = selector;
        this.weight = weight;
    }

    public Filed(){
        name = "title";
        selector="title";
        weight = 1.0f;
    }
}
