package org.apache.tomee.myfaces;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.faces.context.ExternalContext;
import org.apache.myfaces.config.annotation.DefaultAnnotationProvider;
import org.apache.myfaces.shared.util.ClassUtils;
import org.apache.openejb.assembler.classic.WebAppBuilder;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;

public class TomEEAnnotationProvider extends DefaultAnnotationProvider {
    private static final Logger LOGGER = Logger.getInstance(LogCategory.OPENEJB, TomEEAnnotationProvider.class);

    @Override
    public Map<Class<? extends Annotation>, Set<Class<?>>> getAnnotatedClasses(final ExternalContext ctx) {
        final ClassLoader cl = getClassLoader();
        final WebAppBuilder builder = SystemInstance.get().getComponent(WebAppBuilder.class);
        final Map<Class<? extends Annotation>,Set<Class<?>>> map = new HashMap<Class<? extends Annotation>, Set<Class<?>>>();
        final Map<String, Set<String>> scanned = builder.getJsfClasses().get(cl);
        if (scanned == null) {
            return Collections.emptyMap();
        }

        for (Map.Entry<String, Set<String>> entry : scanned.entrySet()) {
            final Class<? extends Annotation> annotation;
            try {
                annotation = (Class<? extends Annotation>) cl.loadClass(entry.getKey());
            } catch (ClassNotFoundException e) {
                continue;
            }

            final Set<String> list = entry.getValue();
            final Set<Class<?>> annotated = new HashSet<Class<?>>(list.size());
            for (String name : list) {
                try {
                    annotated.add(cl.loadClass(name));
                } catch (ClassNotFoundException ignored) {
                    LOGGER.warning("class '" + name + "' was found but can't be loaded as a JSF class");
                }
            }

            map.put(annotation, annotated);
        }
        return map;
    }

    private ClassLoader getClassLoader() {
        final ClassLoader loader = ClassUtils.getContextClassLoader();
        if (loader == null) {
            return getClass().getClassLoader();
        }
        return loader;
    }
}
