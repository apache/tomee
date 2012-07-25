package org.superbiz.jaxws;

import org.apache.ziplock.IO;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.ClassLoaderAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.URL;

import static org.junit.Assert.assertThat;
import static org.junit.internal.matchers.StringContains.containsString;

@RunWith(Arquillian.class)
public class Rot13Test {
    @ArquillianResource
    private URL url;

    @Deployment(testable = false)
    public static WebArchive war() {
        return ShrinkWrap.create(WebArchive.class)
                    .addClass(Rot13.class)
                    .addAsWebInfResource(new ClassLoaderAsset("META-INF/openejb-jar.xml"), ArchivePaths.create("openejb-jar.xml"));
    }

    @Test
    public void checkWSDLIsDeployedWhereItIsConfigured() throws Exception {
        final String wsdl = IO.slurp(new URL(url.toExternalForm() + "tool/rot13?wsdl"));
        assertThat(wsdl, containsString("Rot13"));
    }
}
