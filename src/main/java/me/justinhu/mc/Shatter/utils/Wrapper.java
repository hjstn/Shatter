package me.justinhu.mc.Shatter.utils;

public class Wrapper<T> {
    T object;

    public Wrapper() {
        this.object = null;
    }

    public Wrapper(T object) {
        this.object = object;
    }

    public T get() {
        return this.object;
    }

    public void set(T object) {
        this.object = object;
    }
}
