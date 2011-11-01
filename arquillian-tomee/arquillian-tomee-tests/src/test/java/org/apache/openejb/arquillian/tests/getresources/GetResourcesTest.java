package org.apache.openejb.arquillian.tests.getresources;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.jboss.shrinkwrap.descriptor.api.spec.servlet.web.WebAppDescriptor;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;

import static org.apache.openejb.arquillian.tests.Tests.assertOutput;

/**
 * jira: TOMEE-42.
 *
 * @author rmannibucau
 */
@RunWith(Arquillian.class)
    public class GetResourcesTest {
    public static final String TEST_NAME = GetResourcesTest.class.getSimpleName();

    @Deployment(testable = false) public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class, TEST_NAME + ".war")
                .addClass(GetResourcesServletExporter.class)
                .addClass(GetResourcesListener.class)
                .addClass(GetResourcesHolder.class)
                .addAsWebResource(Thread.currentThread().getContextClassLoader().getResource("test.getresources"), "/config/test.getresources")
                .addAsWebResource(Thread.currentThread().getContextClassLoader().getResource("test.getresources"), "/config/test.getresources2")
                .addAsLibraries(new File("target/test-libs/junit.jar"))
                .setWebXML(new StringAsset(
                      Descriptors.create(WebAppDescriptor.class)
                        .version("3.0").exportAsString()));
    }

    @Test public void check() throws IOException {
        assertOutput("http://localhost:9080/" + TEST_NAME + "/get-resources", "foundFromListener=1");
        assertOutput("http://localhost:9080/" + TEST_NAME + "/get-resources", "servletContextGetResource=ok");
    }
}
