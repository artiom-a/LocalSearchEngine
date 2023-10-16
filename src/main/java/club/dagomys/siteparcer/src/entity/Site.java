package club.dagomys.siteparcer.src.entity;

import club.dagomys.siteparcer.src.dto.Link;
import jakarta.persistence.*;
import lombok.*;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@RequiredArgsConstructor
@ToString
@Entity
@AllArgsConstructor
public class Site implements Serializable, Comparable<Site> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

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

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "site")
    @ToString.Exclude
    private List<Page> pages;

    public Site(Link rootLink) {
        this.rootLink = rootLink;
        this.url = rootLink.getValue();
    }

    @Override
    public int compareTo(@NotNull Site o) {
        return this.getUrl().compareTo(o.getUrl());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Site site = (Site) o;

        if (!url.equals(site.url)) return false;
        if (!name.equals(site.name)) return false;
        return rootLink.equals(site.rootLink);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
