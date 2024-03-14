package club.dagomys.siteparser.src.dto;

import java.util.Set;

public interface Node {
    Set<Link> getChildren();

    String getValue();

    int getStatusCode();

    void addChild(Link child, String relLink);
}
