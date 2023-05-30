package club.dagomys.siteparcer.src.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.jetbrains.annotations.NotNull;
import org.springframework.scheduling.annotation.Async;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Data
@ToString
@Entity
@NoArgsConstructor
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
}
