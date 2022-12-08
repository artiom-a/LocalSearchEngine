package club.dagomys.siteparcer.src.entity.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Validated
public class SearchRequest {
    @NotBlank(message = "Поле не должно быть пустым")
    private String searchLine;
}