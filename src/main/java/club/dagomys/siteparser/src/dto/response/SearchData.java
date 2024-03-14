package club.dagomys.siteparser.src.dto.response;

import club.dagomys.siteparser.src.entity.Page;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.annotation.Immutable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Immutable
public class SearchData implements Comparable<SearchData> {

    @JsonProperty("site")
    private String rootUrl;

    private String siteName;

    private String URI;

    private String title;

    private String snippet;

    private Float relevance;

    public SearchData(Page page, String title, String snippet, float relevance) {
        this.rootUrl = page.getSite().getUrl();
        this.siteName = page.getSite().getName();
        this.URI = page.getRelPath();
        this.title = title;
        this.snippet = snippet;
        this.relevance = relevance;
    }

    @Override
    public int compareTo(@NotNull SearchData o) {
        int res = o.relevance.compareTo(this.relevance);
        if (res == 0 & (o.URI != null & this.URI != null)) {
            res = o.URI.compareTo(this.URI);
        }
        return res;
    }
}

