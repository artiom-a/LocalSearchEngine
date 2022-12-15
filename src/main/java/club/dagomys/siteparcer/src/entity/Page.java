package club.dagomys.siteparcer.src.entity;

import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.io.Serializable;

@Data
@ToString
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(indexes = {@Index(name = "page_index", columnList = "path") })
public class Page implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "path", length = 700)
    private String relPath;
    @Column(name = "code")
    private int statusCode;
    @ToString.Exclude
    @Column(columnDefinition = "MEDIUMTEXT")
    private String content;

    @ManyToOne(cascade=CascadeType.MERGE, fetch = FetchType.EAGER)
    @JoinColumn(name = "site_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Site site;
    @Transient
    private Link link;

    public Page(Link link) {
        this.relPath = link.getRelUrl();
        this.statusCode = link.getStatusCode();
        this.content = link.getHtml();
        this.site = link.getSite();
    }

//    @Override
//    public int compareTo(@NotNull Page o) {
//        return Comparator.comparing(Page::getRelPath)
//                .thenComparing(Page::getContent)
//                .compare(this, o);
//    }
}
