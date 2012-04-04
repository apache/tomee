package org.apache.openejb.maven.plugin.dd;

import org.apache.maven.plugin.logging.Log;

import java.io.File;
import java.net.URL;

public abstract class Merger<T> {
    protected final Log log;

    public Merger(final Log logger) {
        log = logger;
    }

    public abstract T merge(T reference, T toMerge);
    public abstract T createEmpty();
    public abstract T read(URL url);
    public abstract String descriptorName();
    public abstract void dump(File dump, T object) throws Exception ;
}
