package org.example.fuser.ast;

import java.util.ArrayList;
import java.util.List;

public class AccessPath {
    public List<String> path;

    public AccessPath() {
        this.path = new ArrayList<>();
    }

    public void add(int n) {
        this.path.add(String.valueOf(n));
    }

    public void add(String p) {
        this.path.add(p);
    }

    public void prepend(int n) {
        this.path.add(0, String.valueOf(n));
    }

    public void prepend(String p) {
        this.path.add(0, p);
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder("_NODE");
        for(String s : this.path) {
            str.append(".").append(s);
        }
        return str.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj) return true;
        if (obj instanceof AccessPath) {
            AccessPath ap = (AccessPath) obj;
            return path.equals(ap.path);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return path.hashCode();
    }
}

