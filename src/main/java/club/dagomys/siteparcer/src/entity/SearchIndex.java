package club.dagomys.siteparcer.src.entity;

import club.dagomys.siteparcer.src.entity.id.PageId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;

@Data
@ToString
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "`index`")
public class SearchIndex {

    public SearchIndex(Page page, Lemma lemma, float rank){
        this.page = page;
        this.lemma = lemma;
        this.rank = rank;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @JoinColumn(name = "page_id")
    @ManyToOne(fetch = FetchType.EAGER, cascade=CascadeType.MERGE)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Page page;
    @JoinColumn(name = "lemma_id")
    @ManyToOne(fetch = FetchType.EAGER, cascade=CascadeType.MERGE)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Lemma lemma;
    @Column(name = "`rank`")
    private float rank;

}
