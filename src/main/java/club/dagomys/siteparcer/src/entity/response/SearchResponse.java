package club.dagomys.siteparcer.src.entity.response;

import club.dagomys.siteparcer.src.entity.Page;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchResponse {
    private String URI;
    private String title;
    private String snippet;
    private float relevance;
}
