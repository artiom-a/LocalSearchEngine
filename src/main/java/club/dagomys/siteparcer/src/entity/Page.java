package club.dagomys.siteparcer.src.entity;

import club.dagomys.siteparcer.src.dto.Link;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.io.Serializable;

@Getter
@Setter
@RequiredArgsConstructor
@ToString
@Entity
@AllArgsConstructor
@Table(indexes = {@Index(name = "page_index", columnList = "path")})
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

    @ManyToOne(fetch = FetchType.EAGER)
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Page page = (Page) o;

        if (!relPath.equals(page.relPath)) return false;
        if (!site.equals(page.site)) return false;
        return link.equals(page.link);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
