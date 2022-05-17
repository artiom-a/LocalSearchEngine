package club.dagomys.siteparcer.src.entity;

import org.springframework.stereotype.Component;

import java.util.Set;

public interface Node {
    Set<Link> getChildren();
    String getValue();
    int getStatusCode();
    void addChild(Link child);
    void addChild(Link child, String relLink);
}
