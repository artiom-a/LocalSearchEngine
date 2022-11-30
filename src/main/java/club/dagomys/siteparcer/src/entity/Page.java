package club.dagomys.siteparcer.src.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.Type;
import org.jetbrains.annotations.NotNull;

import javax.persistence.*;
import java.util.Comparator;
import java.util.List;

@Data
@ToString
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(indexes = {@Index(name = "page_index", columnList = "path")})
public class Page {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Column(name = "path", length = 700)
    private String relPath;
    @Column(name = "code")
    private int statusCode;
    @ToString.Exclude
    @Column(columnDefinition = "MEDIUMTEXT")
    @Type(type = "org.hibernate.type.TextType")
    private String content;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "site_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Site site;
    @Transient
    private Link link;

    public Page(Link URL) {
        this.link = URL;
        this.relPath = URL.getRelUrl().strip();
        this.statusCode = URL.getStatusCode();
        this.content = URL.getHtml();
        this.site = URL.getSite();

    }

//    @Override
//    public int compareTo(@NotNull Page o) {
//        return Comparator.comparing(Page::getRelPath)
//                .thenComparing(Page::getContent)
//                .compare(this, o);
//    }
}
