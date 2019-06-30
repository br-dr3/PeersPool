package com.github.brdr3.peerspool.util;

public class Tuple<T, T0> {
    T x;
    T0 y;
    
    public Tuple(T x, T0 y) {
        this.x = x;
        this.y = y;
    }

    public T getX() {
        return x;
    }

    public void setX(T x) {
        this.x = x;
    }

    public T0 getY() {
        return y;
    }

    public void setY(T0 y) {
        this.y = y;
    }
    
    @Override
    public String toString() {
        return "(" + x.toString() + ", " + y.toString() + ")";
    }
}
