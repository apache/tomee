package org.superbiz.tomee.arquillian.multiple;

import java.io.IOException;
import java.net.URL;
import org.apache.ziplock.IO;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.container.test.api.TargetsContainer;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

@RunWith(Arquillian.class)
public class MultipleTomEETest {
    @Deployment(name = "war1", testable = false)
    @TargetsContainer("tomee-1")
    public static WebArchive createDep1() {
        return ShrinkWrap.create(WebArchive.class, "application1.war")
                .addAsWebResource(new StringAsset("Hello from TomEE 1"), "index.html");
    }

    @Deployment(name = "war2", testable = false)
    @TargetsContainer("tomee-2")
    public static WebArchive createDep2() {
        return ShrinkWrap.create(WebArchive.class, "application2.war")
                .addAsWebResource(new StringAsset("Hello from TomEE 2"), "index.html");
    }

    @Test
    @OperateOnDeployment("war1")
    public void testRunningInDep1(@ArquillianResource final URL url) throws IOException {
        final String content = IO.slurp(url);
        assertEquals("Hello from TomEE 1", content);
    }

    @Test
    @OperateOnDeployment("war2")
    public void testRunningInDep2(@ArquillianResource final URL url) throws IOException {
        final String content = IO.slurp(url);
        assertEquals("Hello from TomEE 2", content);
    }
}
