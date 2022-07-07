package club.dagomys.siteparcer.src.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.List;

@Data
@ToString
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(indexes = {@Index(name = "page_index", columnList = "path") })
public class Page implements Comparable<Page> {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    @Column(name = "path", length = 700)
    private String relPath;
    @Column(name = "code")
    private int statusCode;
    @Column(columnDefinition = "MEDIUMTEXT")
    @Type(type = "org.hibernate.type.TextType")
    private String content;

    public Page(String URL) {
        this.relPath = URL.strip();
        statusCode = 0;
    }

    @Override
    public int compareTo(Page o) {
        return this.content.compareTo(o.content);
    }
}
