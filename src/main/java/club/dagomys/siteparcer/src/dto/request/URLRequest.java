package club.dagomys.siteparcer.src.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;



@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Validated
public class URLRequest {
    private static final String URL_REGEXP = "(https?://(?:www\\.|(?!www))[a-zA-Z0-9][a-zA-Z0-9-]+[a-zA-Z0-9]\\.\\S{2,}|www\\.[a-zA-Z0-9][a-zA-Z0-9-]+[a-zA-Z0-9]\\.\\S{2,}|https?://(?:www\\.|(?!www))[a-zA-Z0-9]+\\.\\S{2,}|www\\.[a-zA-Z0-9]+\\.\\S{2,})";
    @Pattern(regexp = URL_REGEXP, message = "Введите URL вида http(s)://domain.com")
    @NotBlank(message = "Поле не должно быть пустым")
    private String path;

}