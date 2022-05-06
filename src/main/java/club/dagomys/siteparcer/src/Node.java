package club.dagomys.siteparcer.src;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Set;

public interface Node {
    Set<Link> getChildren();
    String getValue();
    int getStatusCode();
}
