package org.example.fuser.graph;

import java.util.Objects;

public class Edge {

    Vertex v1;
    Vertex v2;

    public Edge(Vertex v1, Vertex v2) {
        this.v1 = v1;
        this.v2 = v2;
    }

    @Override
    public String toString() {
        return "Edge [" + v1 + " -> " + v2 + "]";
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj) return true;
        if(obj instanceof Edge) {
            Edge e = (Edge) obj;
            return v1.equals(e.v1) && v2.equals(e.v2);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(v1, v2);
    }

}

