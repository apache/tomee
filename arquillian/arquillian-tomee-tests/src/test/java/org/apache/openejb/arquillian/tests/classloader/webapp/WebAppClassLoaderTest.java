package org.apache.openejb.arquillian.tests.classloader.webapp;

import org.apache.openejb.arquillian.tests.classloader.HashCdiExtension;
import org.apache.openejb.arquillian.tests.classloader.HashServlet;
import org.apache.openejb.loader.IO;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.enterprise.inject.spi.Extension;
import java.io.IOException;
import java.net.URL;

import static org.junit.Assert.assertEquals;

@RunWith(Arquillian.class)
@RunAsClient
public class WebAppClassLoaderTest {
    @ArquillianResource
    private URL url;

    @Deployment
    public static WebArchive war() {
        return ShrinkWrap.create(WebArchive.class)
                .addClasses(HashCdiExtension.class, HashServlet.class)
                .addAsWebInfResource(EmptyAsset.INSTANCE, ArchivePaths.create("beans.xml"))
                .addAsServiceProvider(Extension.class, HashCdiExtension.class);
    }

    @Test
    public void valid() throws IOException {
        assertEquals("true", IO.slurp(new URL(url.toExternalForm() + "hash")));
    }
}
