package org.apache.openejb.itest.tomee;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import org.apache.openejb.loader.IO;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.ClassLoaderAsset;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;

@RunWith(ITTomEERunner.class)
@Server(name = "server-webapp", cleanWebapp = true, tweaker = ServerWithAWebappTest.WebappTweaker.class)
public class ServerWithAWebappTest {
    @Test
    public void assertWebappIsDeployed() throws IOException {
        final URL url = new URL("http://localhost:" + System.getProperty("tomee.it.server-webapp.http") + "/sample");
        final String str = IO.slurp(url);

        assertNotNull(str);
        System.out.println(str);
        assertThat(str, containsString("Server With A Webapp"));
    }

    public static class WebappTweaker extends SimpleTweaker {
        @Override
        public void tweak(final File home) {
            // to debug:
            // System.setProperty("openejb.server.debug", "true");

            // here we use shrinkwrap to make a quick webapp but
            // maven can be used to retrieved an existing webapp too
            final File warFile = new File("target/sample.war");
            final WebArchive war = ShrinkWrap.create(WebArchive.class, "sample.war")
                    .addAsWebResource(new ClassLoaderAsset("webapp/index.html"), "index.html");
            war.as(ZipExporter.class).exportTo(warFile, true);
            addWebapp(home, warFile);
        }
    }
}
