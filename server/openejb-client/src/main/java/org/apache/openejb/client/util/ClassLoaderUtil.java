package org.apache.openejb.client.util;

public class ClassLoaderUtil {
    private ClassLoaderUtil() {
        // no-op
    }

    public static boolean isParent(final ClassLoader supposedParent, final ClassLoader supposedChild) {
        ClassLoader parent = supposedChild;
        while (parent != null) {
            if (parent == supposedParent) {
                return true;
            }
            parent = parent.getParent();
        }

        return false;
    }
}
