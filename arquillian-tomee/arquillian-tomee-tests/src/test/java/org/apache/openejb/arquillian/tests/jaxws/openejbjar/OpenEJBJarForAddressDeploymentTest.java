package org.apache.openejb.arquillian.tests.jaxws.openejbjar;

import org.apache.ziplock.IO;
import org.apache.ziplock.WebModule;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.URL;

import static org.junit.Assert.assertTrue;

@RunWith(Arquillian.class)
public class OpenEJBJarForAddressDeploymentTest {
    @Deployment(testable = false)
    public static WebArchive createDeployment() {
        return new WebModule(OpenEJBJarForAddressDeploymentTest.class).getArchive();
    }

    @Test
    public void invoke() throws Exception {
        final String s = OpenEJBJarForAddressDeploymentTest.class.getSimpleName();
        final URL url = new URL("http://localhost:" + System.getProperty("tomee.http.port", "11080") + "/" + s + "/webservices/foo/bar/my-ws?wsdl");
        final String wsdl = IO.slurp(url);
        assertTrue(wsdl.contains("LengthCalculator"));
    }
}
