package org.apache.openejb.arquillian.openejb;

import javax.annotation.Resource;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

@RunWith(Arquillian.class)
public class EnvEntriesArquillianStandaloneTest {
    @Resource(name = "foo")
    private String foo;

    @Deployment
    public static JavaArchive archive() {
        return ShrinkWrap.create(JavaArchive.class, EnvEntriesArquillianStandaloneTest.class.getSimpleName().concat(".jar"))
                .addAsManifestResource(EmptyAsset.INSTANCE, ArchivePaths.create("ejb-jar.xml"))
                .addAsManifestResource(new StringAsset("foo=bar"), ArchivePaths.create("env-entries.properties"));
    }

    @Test
    public void check() {
        assertEquals("bar", foo);
    }
}
