package org.superbiz.config;

import org.apache.cxf.jaxrs.client.WebClient;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.URL;

import static org.junit.Assert.assertEquals;

@RunWith(Arquillian.class)
public class PropertiesRestTest {

    @ArquillianResource
    private URL base;

    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class, "test.war")
                .addClasses(PropertiesRest.class)
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Test
    public void testDefaultValue() {
        final String message = WebClient.create(base.toExternalForm())
                .path("/sample/defaultProperty")
                .get(String.class);
        assertEquals("ALOHA", message);
    }

    @Test
    public void testGetJavaVersionFromConfig() {
        final String message = WebClient.create(base.toExternalForm())
                .path("/sample/javaVersion")
                .get(String.class);
        assertEquals(System.getProperty("java.runtime.version"), message);
    }

    @Test
    public void testGetInjectedJavaVersion() {
        final String message = WebClient.create(base.toExternalForm())
                .path("/sample/injectedJavaVersion")
                .get(String.class);
        assertEquals(System.getProperty("java.runtime.version"), message);
    }
}
