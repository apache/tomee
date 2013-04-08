package org.apache.openejb.rest;

import org.apache.openejb.loader.SystemInstance;

import java.io.Serializable;

public class AbstractRestThreadLocalProxy<T> implements Serializable {

    private final ThreadLocal<T> infos = new ThreadLocal<T>();
    private final Class<T> clazz;

    protected AbstractRestThreadLocalProxy(final Class<T> clazz) {
        this.clazz = clazz;
    }

    public T get() {
        T t = infos.get();
        if (t == null) {
            t = find();
        }
        return t;
    }

    public T find() {
        final RESTResourceFinder finder = SystemInstance.get().getComponent(RESTResourceFinder.class);
        if (finder != null) {
            return finder.find(clazz);
        }
        return null;
    }

    public void remove() {
        infos.remove();
    }

    public void set(T value) {
        infos.set(value);
    }
}

