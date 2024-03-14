package club.dagomys.siteparser.src.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.Objects;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Table(indexes = {@Index(name = "lemma_index", columnList = "lemma")})
public class Lemma implements Comparable<Lemma> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String lemma;

    private Integer frequency;

    @ManyToOne(cascade = CascadeType.MERGE, fetch = FetchType.EAGER)
    @JoinColumn(name = "site_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Site site;

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
        if (o == null || this.getClass() != o.getClass()) return false;

        Lemma lemma1 = (Lemma) o;

        return this.lemma.equals(lemma1.lemma) && this.site.equals(lemma1.site) && Objects.equals(this.frequency, lemma1.frequency);
    }


    public Lemma sum(Lemma lemma) {
        lemma.setFrequency(this.frequency + lemma.frequency);
        return lemma;
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
