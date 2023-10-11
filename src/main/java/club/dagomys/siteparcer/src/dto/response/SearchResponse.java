package club.dagomys.siteparcer.src.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchResponse extends Response {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer count;
    @JsonProperty("data")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<SearchData> searchData;
}
