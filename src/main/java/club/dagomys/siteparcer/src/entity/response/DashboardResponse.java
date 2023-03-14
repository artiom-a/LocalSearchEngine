package club.dagomys.siteparcer.src.entity.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {
    private boolean result;
    private Statistic statistics;
}
