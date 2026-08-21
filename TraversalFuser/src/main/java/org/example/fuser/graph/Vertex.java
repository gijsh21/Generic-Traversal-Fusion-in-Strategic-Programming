package org.example.fuser.graph;

import java.util.Objects;

public class Vertex {

    public enum VertexType {
        STATEMENT,
        CALL
    }

    String name;
    String info;

    VertexType type;
    int index;

    public Vertex(String name, VertexType type) {
        this.name = name;
        this.info = "";
        this.type = type;
    }

    public Vertex(String name, String info, VertexType type) {
        this.name = name;
        this.info = info;
        this.type = type;
        this.index = -1;
    }

    public Vertex(String name, String info, VertexType type, int index) {
        this.name = name;
        this.info = info;
        this.type = type;
        this.index = index;
    }

    public String toStringPreferInfo(boolean trimLast, boolean replaceUnderscore) {
        if(info.isEmpty()) return this.toString();
        String res = info;
        if(trimLast) res = res.substring(0, res.length() - 4);
        if(replaceUnderscore) res = res.replace('_', '-');
        return res;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj) return true;
        if(obj instanceof Vertex) {
            Vertex v = (Vertex) obj;
            return name.equals(v.name) && info.equals(v.info) && type.equals(v.type);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, info, type);
    }

}

