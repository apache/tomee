package org.apache.openejb.util.classloader;

public class DefaultClassLoaderComparator implements ClassLoaderComparator {
    private final ClassLoader reference;

    public DefaultClassLoaderComparator(ClassLoader reference) {
        this.reference = reference;
    }

    @Override
    public boolean isSame(final ClassLoader cl) {
        return reference.equals(cl);
    }
}
