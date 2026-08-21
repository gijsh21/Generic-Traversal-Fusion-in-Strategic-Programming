package org.example.fuser.util;

import java.util.Objects;

public class Pair<L, R> {
    private L left;
    private R right;

    private Pair(L left, R right) {
        this.left = left;
        this.right = right;
    }

    public static <L, R> Pair<L, R> of(L left, R right) {
        return new Pair<>(left, right);
    }

    public L getLeft() {
        return left;
    }

    public R getRight() {
        return right;
    }

    public void setLeft(L left) {
        this.left = left;
    }

    public void setRight(R right) {
        this.right = right;
    }

    @Override
    public String toString() {
        return "Pair[" + left + ", " + right + "]";
    }

    @Override
    public int hashCode() {
        return Objects.hash(left, right);
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj) return true;
        if(obj instanceof Pair<?, ?>) {
            Pair<?, ?> p = (Pair<?, ?>) obj;
            return Objects.equals(left, p.getLeft()) && Objects.equals(right, p.getRight());
        }
        return false;
    }
}

