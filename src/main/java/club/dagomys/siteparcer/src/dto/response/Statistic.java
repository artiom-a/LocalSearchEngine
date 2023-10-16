package club.dagomys.siteparcer.src.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Statistic {

    private Total total;

    @JsonProperty("detailed")
    private List<Detail> siteList;

}
