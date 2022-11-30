package club.dagomys.siteparcer.src.entity;

import club.dagomys.siteparcer.src.entity.request.URLRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@ToString
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Site {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String url;
    private String name;
    @Enumerated(EnumType.STRING)
    private SiteStatus status;
    @Column(name = "status_time")
    private LocalDateTime statusTime;
    @Column(name = "last_error")
    private String lastError;
    @Transient
    private Link rootLink;

    public Site(Link rootLink, String name) {
        this.rootLink = rootLink;
        this.url = rootLink.getValue();
        this.name = name;
    }

    public Site(URLRequest URL) {
        this.url = URL.getPath();
        this.name = name;
    }

}
