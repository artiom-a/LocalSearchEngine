package club.dagomys.siteparcer.src.entity.response;

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
    private int count;
    @JsonProperty("data")
    private List<SearchData> searchData;
}
