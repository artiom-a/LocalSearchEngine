package club.dagomys.siteparcer.src;

import java.util.Set;
import java.util.TreeSet;

public class Link implements Node, Comparable<Link> {
    private final String URL;
    private final Set<Link> childSet;
    private Link parentLink;
    private int layer;

    public Link(String URL) {
        layer = 0;
        parentLink = null;
        this.URL = URL;
        childSet = new TreeSet<>();
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
                ", parentLink=" + parentLink +
                ", layer=" + layer +
                '}';
    }
}
