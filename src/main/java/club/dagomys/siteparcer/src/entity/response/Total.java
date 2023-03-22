package club.dagomys.siteparcer.src.entity.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Total {
    @JsonProperty("lemmas")
    private int lemmaCount;
    @JsonProperty("sites")
    private int siteCount;
    @JsonProperty("pages")
    private int pageCount;
    private boolean isIndexing;
}