package club.dagomys.siteparser.src.dto.response;

import club.dagomys.siteparser.src.entity.SiteStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Detail {

    private String url;

    private String name;

    private SiteStatus status;

    private LocalDateTime statusTime;

    private String error;

    @JsonProperty("pages")
    private int pageCount;

    @JsonProperty("lemmas")
    private int lemmaCount;
}
