package org.example.fuser.util;

import java.util.Objects;

public class Triple<L, M, R> {

    private L left;
    private M middle;
    private R right;

    private Triple(L left, M middle, R right) {
        this.left = left;
        this.middle = middle;
        this.right = right;
    }

    public static <L, M, R> Triple<L, M, R> of(L left, M middle, R right) {
        return new Triple<>(left, middle, right);
    }

    public L getLeft() {
        return left;
    }

    public M getMiddle() { return middle; }

    public R getRight() {
        return right;
    }

    public void setLeft(L left) {
        this.left = left;
    }

    public void setMiddle(M middle) { this.middle = middle; }

    public void setRight(R right) {
        this.right = right;
    }

    @Override
    public String toString() {
        return "Triple[" + left + ", " + middle + ", " + right + "]";
    }

    @Override
    public int hashCode() {
        return Objects.hash(left, middle, right);
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj) return true;
        if(obj instanceof Triple<?, ?, ?>) {
            Triple<?, ?, ?> t = (Triple<?, ?, ?>) obj;
            return Objects.equals(left, t.getLeft())
                    && Objects.equals(middle, t.getMiddle())
                    && Objects.equals(right, t.getRight());
        }
        return false;
    }

}


