package club.dagomys.siteparcer.src.entity.response;

import club.dagomys.siteparcer.src.entity.Page;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchResponse extends Response {
    private int count;
    @JsonProperty("data")
    private List<SearchData> searchData;
}
