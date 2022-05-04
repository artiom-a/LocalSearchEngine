package club.dagomys.siteparcer.src;

import java.util.Set;

public interface Node {
    Set<Link> getChildren();
    String getValue();
    int getStatusCode();
}
