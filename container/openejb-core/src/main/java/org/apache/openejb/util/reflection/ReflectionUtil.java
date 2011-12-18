package org.apache.openejb.util.reflection;

import org.apache.openejb.OpenEJBException;

import java.lang.reflect.Field;

public final class ReflectionUtil {
    private ReflectionUtil() {
        // no-op
    }

    public static void set(Object notNullInstance, String fieldName, Object value) throws OpenEJBException {
        Field field = null;
        boolean accessible = true;
        try {
            Class<?> current = notNullInstance.getClass();
            while (!current.equals(Object.class) && field == null) {
                field = current.getDeclaredField(fieldName);
                current = current.getSuperclass();
            }
            accessible = field.isAccessible();
            if (!accessible) {
                field.setAccessible(true);
            }
            field.set(notNullInstance, value);
        } catch (Exception cce) {
            throw new OpenEJBException(cce);
        } finally {
            if (field != null) {
                field.setAccessible(accessible);
            }
        }
    }
}
