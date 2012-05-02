package org.apache.openejb.arquillian.openejb;

import javax.ejb.Singleton;
import javax.inject.Inject;
import org.apache.openejb.loader.SystemInstance;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(Arquillian.class)
public class CDIArquillianStandaloneTest {
    @Inject
    private ABean bean;

    @Inject
    private AnEJB ejbFromCdiAnnotation;

    @Deployment
    public static JavaArchive archive() {
        return ShrinkWrap.create(JavaArchive.class, CDIArquillianStandaloneTest.class.getSimpleName().concat(".jar"))
                    .addClasses(ABean.class, AnEJB.class)
                    .addAsManifestResource(EmptyAsset.INSTANCE, ArchivePaths.create("beans.xml"));
    }

    @Test
    public void checkItIsStarted() {
        assertTrue(SystemInstance.isInitialized());
    }

    @Test
    public void checkInjections() {
        assertNotNull(bean);
        assertNotNull(ejbFromCdiAnnotation);
    }

    public static class ABean {}

    @Singleton public static class AnEJB {}
}
