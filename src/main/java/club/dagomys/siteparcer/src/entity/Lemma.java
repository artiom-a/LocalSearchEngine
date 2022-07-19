package club.dagomys.siteparcer.src.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@ToString
public class Lemma implements Comparable<Lemma> {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String lemma;
    private int frequency;

    public Lemma(String lemma, int frequency) {
        this.lemma = lemma;
        this.frequency = frequency;
    }

    @Override
    public int compareTo(Lemma o) {
        return this.getLemma().compareTo(o.getLemma());
    }
}
