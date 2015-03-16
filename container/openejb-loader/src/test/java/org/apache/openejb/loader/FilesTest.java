package org.apache.openejb.loader;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class FilesTest {

    private final static File file = new File("target/test/foo.jar");

    @Test
    public void testDelete() throws Exception {
        doDelete(false);
    }

    @Test
    public void testRemove() throws Exception {
        doDelete(true);
    }

    private void doDelete(final boolean remove) throws IOException {

        final long start = System.nanoTime();

        for (int i = 0; i < 20; i++) {

            if (remove) {
                Files.remove(file);
            } else {
                Files.delete(file);
            }

            Files.mkdirs(file.getParentFile());
            assertTrue(file.createNewFile());
            assertTrue(file.exists());
        }

        assertTrue(file.getParentFile().exists());

        if (remove) {
            Files.remove(file.getParentFile());
        } else {
            Files.delete(file.getParentFile());
        }

        assertFalse(file.exists());
        assertFalse(file.getParentFile().exists());

        final long time = TimeUnit.MILLISECONDS.convert(System.nanoTime() - start, TimeUnit.NANOSECONDS);
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, String.format("Completed File.%1$s in %2$sms" , remove ? "remove" : "delete", String.valueOf(time)));
    }
}