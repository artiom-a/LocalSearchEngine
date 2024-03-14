package club.dagomys.siteparser.src.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class Response {

    private boolean result;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String error;
}
