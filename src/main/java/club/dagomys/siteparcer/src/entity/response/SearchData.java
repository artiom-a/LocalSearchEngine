package club.dagomys.siteparcer.src.entity.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchData {
    @JsonProperty("site")
    private String rootUrl;
    private String siteName;
    private String URI;
    private String title;
    private String snippet;
    private float relevance;
}
