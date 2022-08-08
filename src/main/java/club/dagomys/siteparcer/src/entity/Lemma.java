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
@Table(indexes = {@Index(name = "lemma_index", columnList = "lemma") })
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Lemma lemma1 = (Lemma) o;

        return lemma != null ? lemma.equals(lemma1.lemma) : lemma1.lemma == null;
    }


    public Lemma sum(Lemma lemma) {
        lemma.setFrequency(this.frequency + lemma.frequency);
        return lemma;
    }
}
