package org.apache.openejb.util;

import java.lang.annotation.Annotation;

/**
 * @author rmannibucau
 */
public final class AnnotationUtil {
    private AnnotationUtil() {
        // no-op
    }

    public static Annotation getAnnotation(Class<? extends Annotation> a, Class<?> clazz) {
        Class<?> current = clazz;
        do {
            Annotation annotation = current.getAnnotation(a);
            if (annotation != null) {
                return annotation;
            }
            current = current.getSuperclass();
        } while (current != null);
        return null;
    }
}
