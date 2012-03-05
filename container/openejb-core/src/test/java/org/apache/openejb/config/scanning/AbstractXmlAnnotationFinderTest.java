package org.apache.openejb.config.scanning;

import org.apache.openejb.config.ConfigurableClasspathArchive;
import org.apache.openejb.config.scanning.bean.MyAnnotation;
import org.apache.openejb.config.scanning.bean.MyBean1;
import org.apache.openejb.config.scanning.bean.MyBean2;
import org.apache.openejb.config.scanning.bean.MyBean3;
import org.apache.xbean.finder.AnnotationFinder;
import org.apache.xbean.finder.IAnnotationFinder;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public abstract class AbstractXmlAnnotationFinderTest {
    protected IAnnotationFinder finder;

    @Before
    public void initFinder() throws Exception {
        final ClassLoader loader = Thread.currentThread().getContextClassLoader();
        System.setProperty("openejb.scan.xml.name", scanXml());
        finder = new AnnotationFinder(new ConfigurableClasspathArchive(loader,
                Arrays.asList(
                        new URL(loader.getResource(scanXml()).toExternalForm().replace(scanXml(), ""))
                )
        ));
        System.clearProperty("openejb.scan.xml.name");
    }

    protected abstract String scanXml();

    @Test
    public void findClass() {
        final List<Class<?>> myClassAnnotated = finder.findAnnotatedClasses(MyAnnotation.class);
        assertEquals(1, myClassAnnotated.size());
        assertEquals(MyBean1.class, myClassAnnotated.iterator().next());
    }

    @Test
    public void findMethod() {
        final List<Method> myMethodAnnotated = finder.findAnnotatedMethods(MyAnnotation.class);
        assertEquals(1, myMethodAnnotated.size());
        final Method method = myMethodAnnotated.iterator().next();
        assertEquals(MyBean2.class, method.getDeclaringClass());
        assertEquals("aMethod", method.getName());
    }

    @Test
    public void findField() {
        final List<Field> myFieldAnnotated = finder.findAnnotatedFields(MyAnnotation.class);
        assertEquals(1, myFieldAnnotated.size());
        final Field field = myFieldAnnotated.iterator().next();
        assertEquals(MyBean3.class, field.getDeclaringClass());
        assertEquals("aField", field.getName());
    }
}
