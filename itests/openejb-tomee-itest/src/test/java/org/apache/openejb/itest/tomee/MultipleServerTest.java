package org.apache.openejb.itest.tomee;

import java.net.URL;
import org.apache.openejb.loader.IO;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;

@Servers({
        @Server(name = "tomee1"),
        @Server(name = "tomee2"),
        @Server(name = "tomee3")
})
@RunWith(ITTomEERunner.class)
public class MultipleServerTest {
    @Test // any client test
    public void checkServersAreOn() throws Exception {
        for (int i = 1; i <= 3; i++) {
            final URL url = new URL("http://localhost:" + System.getProperty("tomee.it.tomee" + i + ".http") + "/tomee");
            final String str = IO.slurp(url);

            assertNotNull(str);
            assertThat(str, containsString("Welcome to Apache TomEE"));
        }
    }
}

