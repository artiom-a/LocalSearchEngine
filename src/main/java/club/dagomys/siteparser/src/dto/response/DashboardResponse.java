package club.dagomys.siteparser.src.dto.response;

import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse extends Response {

    private Statistic statistics;
}
