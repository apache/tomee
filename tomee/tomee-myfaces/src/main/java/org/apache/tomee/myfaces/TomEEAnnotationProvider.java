package org.apache.tomee.myfaces;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.faces.bean.ManagedBean;
import javax.faces.component.FacesComponent;
import javax.faces.component.behavior.FacesBehavior;
import javax.faces.context.ExternalContext;
import javax.faces.convert.FacesConverter;
import javax.faces.event.NamedEvent;
import javax.faces.render.FacesBehaviorRenderer;
import javax.faces.render.FacesRenderer;
import javax.faces.validator.FacesValidator;
import org.apache.myfaces.config.annotation.DefaultAnnotationProvider;
import org.apache.myfaces.shared.util.ClassUtils;
import org.apache.openejb.config.FinderFactory;
import org.apache.xbean.finder.Annotated;
import org.apache.xbean.finder.IAnnotationFinder;

public class TomEEAnnotationProvider extends DefaultAnnotationProvider {
    private static final Class<?>[] JSF_CLASSES = new Class<?>[] {
            FacesComponent.class, FacesBehavior.class, FacesConverter.class,
            FacesValidator.class, FacesRenderer.class, ManagedBean.class,
            NamedEvent.class, FacesBehaviorRenderer.class
    };

    @Override
    public Map<Class<? extends Annotation>, Set<Class<?>>> getAnnotatedClasses(final ExternalContext ctx) {
        final ClassLoader cl = getClassLoader();
        final IAnnotationFinder finder = FinderFactory.getFinder(cl);
        if (finder == null) { // our scanning is probably too slow compared to myfaces one
            return super.getAnnotatedClasses(ctx);
        }

        final Map<Class<? extends Annotation>,Set<Class<?>>> map = new HashMap<Class<? extends Annotation>, Set<Class<?>>>();
        for (Class<?> clazz : JSF_CLASSES) {
            map.put((Class<? extends Annotation>) clazz, scannedClasses(finder, clazz));
        }
        return map;
    }

    private Set<Class<?>> scannedClasses(final IAnnotationFinder finder, final Class<?> clazz) {
        final Set<Class<?>> classes = new HashSet<Class<?>>();
        final List<Annotated<Class<?>>> found = finder.findMetaAnnotatedClasses((Class<? extends Annotation>) clazz);
        for (Annotated<Class<?>> foundClass : found) {
            classes.add(foundClass.get());
        }
        return classes;
    }

    private ClassLoader getClassLoader() {
        final ClassLoader loader = ClassUtils.getContextClassLoader();
        if (loader == null) {
            return getClass().getClassLoader();
        }
        return loader;
    }
}
