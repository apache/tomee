package org.apache.openejb.itest.tomee;

import org.apache.ziplock.JarLocation;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

import static org.junit.Assert.assertTrue;

@RunWith(ITTomEERunner.class)
@Server(name = "server")
public class LibraryWrappedTest {
    @Library("server")
    public static File lib() {
        return JarLocation.jarLocation(ShrinkWrap.class);
    }

    @Test
    public void checkSWWasAddedToTheContainer() {
        assertTrue(new File("target/it-working-dir/server/lib/shrinkwrap-api-1.0.0.jar").exists());
    }
}
