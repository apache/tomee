package org.apache.openejb.arquillian.openejb;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.inject.Inject;
import javax.sql.DataSource;
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
public class SimpleArquillianStandaloneTest {
    @Inject
    private ABean bean;

    @Inject
    private AnEJB ejbFromCdiAnnotation;

    @EJB
    private AnEJB ejbFromEjbAnnotation;

    @Resource
    private DataSource defaultDs;

    @Deployment
    public static JavaArchive archive() {
        return ShrinkWrap.create(JavaArchive.class, SimpleArquillianStandaloneTest.class.getSimpleName().concat(".jar"))
                    .addClass(ABean.class)
                    .addAsManifestResource(EmptyAsset.INSTANCE, ArchivePaths.create("ejb-jar.xml"))
                    .addAsManifestResource(EmptyAsset.INSTANCE, ArchivePaths.create("beans.xml"));
    }

    @Test
    public void checkItIsStarted() {
        assertTrue(SystemInstance.isInitialized());
    }

    @Test
    public void checkInjection() {
        assertNotNull(bean);
        assertNotNull(ejbFromCdiAnnotation);
        assertNotNull(ejbFromEjbAnnotation);
        assertNotNull(defaultDs);
    }

    public static class ABean {}

    @Singleton public static class AnEJB {}
}
