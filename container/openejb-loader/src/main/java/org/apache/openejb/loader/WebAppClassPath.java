package org.apache.openejb.loader;

public class WebAppClassPath extends TomcatClassPath {

    public WebAppClassPath() {
        this(getContextClassLoader());
    }

    public WebAppClassPath(ClassLoader classLoader) {
        super(classLoader);
    }

    protected void rebuild() {
    }
}
