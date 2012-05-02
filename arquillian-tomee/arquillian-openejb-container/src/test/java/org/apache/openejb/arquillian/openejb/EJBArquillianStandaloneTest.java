package org.apache.openejb.arquillian.openejb;

import javax.ejb.EJB;
import javax.ejb.Singleton;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertNotNull;

@RunWith(Arquillian.class)
public class EJBArquillianStandaloneTest {
    @EJB
    private AnEJB ejbFromEjbAnnotation;

    @Deployment
    public static JavaArchive archive() {
        return ShrinkWrap.create(JavaArchive.class, EJBArquillianStandaloneTest.class.getSimpleName().concat(".jar"))
                .addClass(AnEJB.class)
                .addAsManifestResource(EmptyAsset.INSTANCE, ArchivePaths.create("ejb-jar.xml"));
    }

    @Test
    public void checkInjections() {
        assertNotNull(ejbFromEjbAnnotation);
    }

    @Singleton
    public static class AnEJB {}
}
