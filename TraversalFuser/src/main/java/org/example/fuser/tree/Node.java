package org.example.fuser.tree;

import org.spoofax.interpreter.terms.IStrategoTerm;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Node {

    public String name;
    public List<Object> children;

    public Node(String name) {
        this.name = name;
        this.children = new ArrayList<>();
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder(this.name);

        if(this.children == null || this.children.isEmpty()) {
            str.append("()");
            return str.toString();
        }

        str.append("(");
        for(int i = 0; i < this.children.size() - 1; i++) {
            str.append(this.children.get(i).toString());
            str.append(", ");
        }

        str.append(this.children.get(this.children.size() - 1).toString());
        str.append(")");
        return str.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj) return true;
        if (obj instanceof Node) {
            Node n = (Node) obj;
            return this.name.equals(n.name) && this.children.equals(n.children);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.name, this.children);
    }

}

