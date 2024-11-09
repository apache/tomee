package org.apache.openejb.arquillian.tests.jaxrs.servlets.welcomefile;

import org.apache.openejb.arquillian.tests.jaxrs.JaxrsTest;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.jboss.shrinkwrap.descriptor.api.webapp30.WebAppDescriptor;
import org.jboss.shrinkwrap.descriptor.api.webcommon30.WebAppVersionType;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

@RunWith(Arquillian.class)
public class ServletsWithWelcomeFileTest extends JaxrsTest {
    @Deployment(testable = false)
    public static WebArchive createDeployment() {
        WebAppDescriptor webXml = Descriptors.create(WebAppDescriptor.class)
                .version(WebAppVersionType._3_0)
                .createServlet()
                    .servletName("My Filetype Servlet")
                    .servletClass(MyFiletypeServlet.class.getName())
                .up()
                .createServletMapping()
                    .servletName("My Filetype Servlet")
                    .urlPattern("*.mine")
                .up()
                .createWelcomeFileList()
                    .welcomeFile("index.mine")
                .up();

        return ShrinkWrap.create(WebArchive.class)
                .addClasses(MyRestApi.class, MyRestApplication.class, MyFiletypeServlet.class)
                .setWebXML(new StringAsset(webXml.exportAsString()));
    }

    @Test
    public void welcomeFileOnRoot() throws IOException {
        assertEquals("filetype matched", get("/"));
    }

    @Test
    public void fileExtension() throws IOException {
        assertEquals("filetype matched", get("/test.mine"));
    }

    @Test
    public void jaxRsWithTrailingSlash() throws IOException {
        assertEquals("Hello world!", get("/api/test/"));
    }
}
