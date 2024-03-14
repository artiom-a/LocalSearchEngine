package club.dagomys.siteparser.src.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Validated
public class SearchRequest {

    @NotBlank(message = "Поле не должно быть пустым")
    private String searchLine;
}
