package club.dagomys.siteparcer.src.dto;

import club.dagomys.siteparcer.src.entity.Site;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.TreeSet;

@Component
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Link implements Node, Comparable<Link> {

    private String URL;

    private String relUrl;

    private Set<Link> childSet;

    private Link parentLink;

    private int layer;

    private int statusCode;

    private String html;

    private Site site;

    public Link(String URL) {
        layer = 0;
        parentLink = null;
        this.URL = URL.strip();
        childSet = new TreeSet<>();
        statusCode = 0;
    }

    int setLayer() {
        int depth = 0;
        if (parentLink == null) {
            return this.layer = depth;
        } else {
            depth += 1 + parentLink.setLayer();
            return depth;
        }
    }

    @Override
    public void addChild(Link child, String relLink) {
        Link root = getRootLink();
        if (root.getLayer() == 0) {
            root.setRelUrl("/");
        }
        if (!root.contains(child.getValue())) {
            child.setParentLink(this);
            child.setRelUrl(relLink);
            childSet.add(child);
        }
    }


    private void setParentLink(Link parent) {
        this.parentLink = parent;
        this.layer = setLayer();
    }

    private boolean contains(String url) {
        if (this.URL.contains(url)) {
            return true;
        } else {
            for (Link child : childSet) {
                if (child.contains(url)) {
                    return true;
                }
            }
        }
        return false;

    }

    @Override
    public int getStatusCode() {
        return statusCode;
    }


    private Link getRootLink() {
        return parentLink == null ? this : parentLink.getRootLink();
    }

    @Override
    public Set<Link> getChildren() {
        return childSet;
    }


    @Override
    public String getValue() {
        return URL;
    }

    @Override
    public int compareTo(Link o) {
        return this.URL.compareTo(o.URL);
    }

    @Override
    public String toString() {
        return "Link{" +
                "URL='" + URL + '\'' +
                ", childSet size=" + childSet.size() +
                ", relative URL =" + relUrl +
                ", status=" + statusCode +
                ", parentLink=" + parentLink +
                ", layer=" + layer +
                '}';
    }
}
