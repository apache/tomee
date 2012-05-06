package org.apache.openejb.itest.tomee;

import org.apache.openejb.loader.IO;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.net.URL;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;

@Servers({
        @Server(name = "tomee1"),
        @Server(name = "tomee2")
})
@RunWith(ITTomEERunner.class)
public class ShrinkWrappedTest {
    @Archive("tomee1")
    public static WebArchive war1() {
        return ShrinkWrap.create(WebArchive.class, "test.war").addAsWebResource(new StringAsset("tomee1"), "index.html");
    }

    @Archive("tomee2")
    public static WebArchive war2() {
        return ShrinkWrap.create(WebArchive.class, "test.war").addAsWebResource(new StringAsset("tomee2"), "index.html");
    }

    @Test
    public void check() throws IOException {
        for (int i = 1; i <= 2; i++) {
            final URL url = new URL("http://localhost:" + System.getProperty("tomee.it.tomee" + i + ".http") + "/test");
            final String str = IO.slurp(url);

            assertNotNull(str);
            assertThat(str, containsString("tomee" + i));
        }
    }
}
