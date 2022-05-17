package club.dagomys.siteparcer.src.entity;

import lombok.Data;
import lombok.ToString;

import java.util.Set;
import java.util.TreeSet;

@Data
@ToString
public class Page implements Node, Comparable<Page> {

    private int id;
    private final String URL;
    private String relPath;
    @ToString.Exclude
    private final Set<Link> childSet;
    private Link parentLink;
    private int layer;
    private int statusCode;
    @ToString.Exclude
    private StringBuilder content;

    public Page(String URL) {
        layer = 0;
        parentLink = null;
        this.URL = URL.strip();
        childSet = new TreeSet<>();
        statusCode = 0;
    }

    public String getURL() {
        return URL;
    }

    public String getRelPath() {
        return relPath;
    }

    public Set<Link> getChildSet() {
        return childSet;
    }

    public Link getParentLink() {
        return parentLink;
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

    public StringBuilder getContent() {
        return content;
    }

    @Override
    public Set<Link> getChildren() {
        return null;
    }

    @Override
    public String getValue() {
        return null;
    }

    @Override
    public int getStatusCode() {
        return 0;
    }

    @Override
    public void addChild(Link child) {

    }

    @Override
    public void addChild(Link child, String relLink) {

    }

    @Override
    public int compareTo(Page o) {
        return 0;
    }
}
