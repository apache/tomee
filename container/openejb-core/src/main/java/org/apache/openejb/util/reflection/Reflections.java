package org.apache.openejb.util.reflection;

import java.lang.reflect.Method;

public final class Reflections {
    private Reflections() {
        // no-op
    }

    public static Object invokeByReflection(final Object obj, final String mtdName, final Class<?>[] paramTypes, final Object[] args) {
        Method mtd;
        try {
            mtd = obj.getClass().getDeclaredMethod(mtdName, paramTypes);
            return mtd.invoke(obj, args);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }
}
