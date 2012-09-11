package org.apache.openejb.arquillian.openejb;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import static org.junit.Assert.assertNotNull;

@RunWith(Arquillian.class)
public class ArquillianAndMockitoTest {
    @Inject
    private AFooBean bean;

    @Mock @Produces
    private static AnInterface mock;

    @Deployment
    public static JavaArchive archive() {
        return ShrinkWrap.create(JavaArchive.class, ArquillianAndMockitoTest.class.getSimpleName().concat(".jar"))
                .addClasses(AnInterface.class, AFooBean.class)
                .addAsManifestResource(EmptyAsset.INSTANCE, ArchivePaths.create("beans.xml"));
    }

    @Test
    public void mockWorks() {
        assertNotNull(bean);
        assertNotNull(mock);
        assertNotNull(bean.get());
    }

    public static interface AnInterface {}

    public static class AFooBean {
        @Inject
        private AnInterface mock;

        public AnInterface get() {
            return mock;
        }
    }
}
