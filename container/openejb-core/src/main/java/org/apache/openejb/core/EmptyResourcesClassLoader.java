package org.apache.openejb.core;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;

public class EmptyResourcesClassLoader extends ClassLoader {
    public EmptyResourcesClassLoader() {
        super(null);
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        return new Enumeration<URL>() {
            @Override
            public boolean hasMoreElements() {
                return false;
            }

            @Override
            public URL nextElement() {
                throw new UnsupportedOperationException("this enumeration has no elements");
            }
        };
    }
}
