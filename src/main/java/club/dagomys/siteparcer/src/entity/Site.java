package club.dagomys.siteparcer.src.entity;

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

    public Site(String url, String name) {
        this.url = url;
        this.name = name;

    }

}
