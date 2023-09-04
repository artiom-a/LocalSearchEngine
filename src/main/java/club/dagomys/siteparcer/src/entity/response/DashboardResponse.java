package club.dagomys.siteparcer.src.entity.response;

import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse extends Response {
    private Statistic statistics;
}
