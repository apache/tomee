package org.superbiz.embedded.remote;

import java.io.IOException;
import java.net.URL;
import org.apache.ziplock.IO;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.superbiz.SomeRest;

import static org.junit.Assert.assertEquals;

@Category(EmbeddedRemote.class)
@RunWith(Arquillian.class)
public class OpenEJBEmbeddedRemoteTest {
    @Deployment
    public static JavaArchive jar() {
        return ShrinkWrap.create(JavaArchive.class, "my-webapp.jar").addClass(SomeRest.class);
    }

    @Test
    public void check() throws IOException {
        final String content = IO.slurp(new URL("http://localhost:4204/my-webapp/rest/ok"));
        assertEquals("rest", content);
    }
}
