package club.dagomys.siteparcer.src;

import java.util.Set;
import java.util.TreeSet;

public class Link implements Node, Comparable<Link> {
    private final String URL;
    private final Set<Link> childSet;
    private final Set<Link> childRelLink;
    private Link parentLink;
    private Link relLink;
    private int layer;
    private int statusCode;

    public Link(String URL) {
        layer = 0;
        parentLink = null;
        this.URL = URL;
        childSet = new TreeSet<>();
        childRelLink = new TreeSet<>();
        statusCode = 0;
    }

    public int getLayer() {
        return layer;
    }

    private int setLayer() {
        int depth = 0;
        if (parentLink == null) {
            return this.layer = depth;
        } else {
            depth += 1 + parentLink.setLayer();
            return depth;
        }
    }

    public void addChild(Link child) {
        Link root = getRootLink();
        if (!root.contains(child.getValue())) {
            child.setParentLink(this);
            childSet.add(child);
        }
    }

    private void setParentLink(Link parent) {
        this.parentLink = parent;
        this.relLink = parent.relLink;
        this.layer = setLayer();
    }

    public boolean contains(String url) {
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

    public Link getAbsLink() {
        return this;
    }

    @Override
    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    private Link getRootLink() {
        return parentLink == null ? this : parentLink.getRootLink();
    }

    @Override
    public Set<Link> getChildren() {
        return childSet;
    }

    public Set<Link> getRelChildren() {
        return childRelLink;
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
                ", rel list =" + childRelLink.size() +
                ", status=" + statusCode +
                ", parentLink=" + parentLink +
                ", layer=" + layer +
                '}';
    }
}
