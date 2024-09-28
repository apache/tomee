package org.apache.openejb.arquillian.tests.jaxrs.faces;

import org.apache.openejb.arquillian.tests.jaxrs.JaxrsTest;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.jboss.shrinkwrap.descriptor.api.facesconfig22.WebFacesConfigDescriptor;
import org.jboss.shrinkwrap.descriptor.api.webapp30.WebAppDescriptor;
import org.jboss.shrinkwrap.descriptor.api.webcommon30.WebAppVersionType;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

// Test that reproduces https://issues.apache.org/jira/browse/TOMEE-4406
@RunWith(Arquillian.class)
public class AvoidConflictWithFacesTest extends JaxrsTest {
    @Deployment(testable = false)
    public static WebArchive createDeployment() {
        WebFacesConfigDescriptor facesConfig = Descriptors.create(WebFacesConfigDescriptor.class);

        WebAppDescriptor webXml = Descriptors.create(WebAppDescriptor.class)
                .version(WebAppVersionType._3_0)
                .createServlet()
                    .servletName("Faces Servlet")
                    .servletClass("jakarta.faces.webapp.FacesServlet")
                .up()
                .createServletMapping()
                    .servletName("Faces Servlet")
                    .urlPattern("*.xhtml")
                .up();

        WebArchive war = ShrinkWrap.create(WebArchive.class)
                .addClasses(MyRestApi.class, MyRestApplication.class)
                .setWebXML(new StringAsset(webXml.exportAsString()))
                .addAsWebInfResource(new StringAsset(facesConfig.exportAsString()), "faces-config.xml")
                .addAsResource(new StringAsset("Hello from Faces"), "META-INF/resources/my-resources/hello.txt");

        System.err.println(war.toString(true));
        return war;
    }

    @Test
    public void facesResourceAccessible() throws IOException {
        assertEquals("Hello from Faces", get("/jakarta.faces.resources/hello.txt.xhtml?ln=my-resources"));
    }

    @Test
    public void validateJaxrsDeployed() throws IOException {
        assertEquals("Hello world!", get("/api/test"));
    }
}
