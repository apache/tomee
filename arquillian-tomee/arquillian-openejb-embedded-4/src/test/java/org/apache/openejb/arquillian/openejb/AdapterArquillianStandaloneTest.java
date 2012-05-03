package org.apache.openejb.arquillian.openejb;

import org.apache.openejb.loader.SystemInstance;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertTrue;

@RunWith(Arquillian.class)
public class AdapterArquillianStandaloneTest {
    @Deployment
    public static JavaArchive archive() {
        return ShrinkWrap.create(JavaArchive.class, AdapterArquillianStandaloneTest.class.getSimpleName().concat(".jar"))
                    .addAsManifestResource(EmptyAsset.INSTANCE, ArchivePaths.create("beans.xml"));
    }

    @Test
    public void checkItIsStarted() {
        assertTrue(SystemInstance.isInitialized());
    }
}
