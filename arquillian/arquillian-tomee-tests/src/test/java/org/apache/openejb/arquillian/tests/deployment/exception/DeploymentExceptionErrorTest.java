package org.apache.openejb.arquillian.tests.deployment.exception;

import org.apache.openejb.OpenEJBException;
import org.apache.webbeans.exception.WebBeansConfigurationException;
import org.jboss.arquillian.container.spi.client.container.DeploymentException;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.ShouldThrowException;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.jboss.shrinkwrap.descriptor.api.beans10.BeansDescriptor;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertNotNull;

@RunWith(Arquillian.class)
public class DeploymentExceptionErrorTest {
    @ArquillianResource
    private DeploymentException de;

    @ArquillianResource
    private WebBeansConfigurationException owbException;

    @ArquillianResource
    private OpenEJBException oejbException;

    @Deployment(testable = false)
    @ShouldThrowException(DeploymentException.class)
    public static WebArchive war() {
        return ShrinkWrap.create(WebArchive.class)
                    .addAsWebInfResource(new StringAsset(Descriptors.create(BeansDescriptor.class)
                            .createInterceptors()
                                .clazz("i.dont.exist.so.i.ll.make.the.deployment.fail")
                            .up()
                            .exportAsString()), ArchivePaths.create("beans.xml"));
    }

    @Test
    public void checkSomeExceptionsOfTheHierarchy() {
        assertNotNull(de);
        assertNotNull(owbException);
        assertNotNull(oejbException);
    }
}
