package org.apache.tomee.myfaces;

import org.apache.myfaces.shared.util.ClassUtils;
import org.apache.myfaces.spi.AnnotationProvider;
import org.apache.myfaces.view.facelets.util.Classpath;

import javax.faces.bean.ManagedBean;
import javax.faces.component.FacesComponent;
import javax.faces.component.behavior.FacesBehavior;
import javax.faces.context.ExternalContext;
import javax.faces.convert.FacesConverter;
import javax.faces.event.NamedEvent;
import javax.faces.render.FacesBehaviorRenderer;
import javax.faces.render.FacesRenderer;
import javax.faces.validator.FacesValidator;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TomEEAnnotationProvider extends AnnotationProvider {
    private static final Class<?>[] JSF_CLASSES = new Class<?>[] {
            FacesComponent.class, FacesBehavior.class, FacesConverter.class,
            FacesValidator.class, FacesRenderer.class, ManagedBean.class,
            NamedEvent.class, FacesBehaviorRenderer.class
    };

    @Override
    public Map<Class<? extends Annotation>, Set<Class<?>>> getAnnotatedClasses(final ExternalContext ctx) {
        final Map<Class<? extends Annotation>,Set<Class<?>>> map = new HashMap<Class<? extends Annotation>, Set<Class<?>>>();
        for (Class<?> clazz : JSF_CLASSES) {
            map.put((Class<? extends Annotation>) clazz, scannedClasses(clazz));
        }
        return map;
    }

    private Set<Class<?>> scannedClasses(final Class<?> clazz) {
        // TODO: get scanned classes from xbean finders
        return null;
    }

    @Override
    public Set<URL> getBaseUrls() throws IOException {
        final Set<URL> urlSet = new HashSet<URL>();
        final ClassLoader loader = getClassLoader();

        // TODO: use xbean to find resources

        //This usually happens when maven-jetty-plugin is used
        //Scan jars looking for paths including META-INF/faces-config.xml
        final Enumeration<URL> resources = loader.getResources("META-INF/faces-config.xml");
        while (resources.hasMoreElements())
        {
            urlSet.add(resources.nextElement());
        }

        //Scan files inside META-INF ending with .faces-config.xml
        final URL[] urls = Classpath.search(loader, "META-INF/", ".faces-config.xml");
        for (int i = 0; i < urls.length; i++) {
            urlSet.add(urls[i]);
        }

        return urlSet;
    }

    private ClassLoader getClassLoader() {
        final ClassLoader loader = ClassUtils.getContextClassLoader();
        if (loader == null) {
            return getClass().getClassLoader();
        }
        return loader;
    }
}
