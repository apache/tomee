package org.apache.tomee.embedded;

import org.apache.openejb.OpenEjbContainer;
import org.apache.openejb.util.IOUtils;
import org.junit.Test;

import javax.ejb.embeddable.EJBContainer;
import javax.enterprise.inject.spi.BeanManager;
import javax.naming.Context;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;
import java.util.jar.JarFile;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

/**
 * @author rmannibucau
 */
public class EmbeddedTomEEContainerTest {
    @Test(expected = OpenEjbContainer.NoModulesFoundException.class) public void noModule() {
        Properties p = new Properties();
        p.setProperty(EJBContainer.APP_NAME, "test");
        p.setProperty(EJBContainer.PROVIDER, EmbeddedTomEEContainer.class.getName());
        EJBContainer.createEJBContainer(p);
    }

    @Test public void containerTest() throws Exception {
        Properties p = new Properties();
        p.setProperty(EJBContainer.APP_NAME, "test");
        p.setProperty(EJBContainer.PROVIDER, EmbeddedTomEEContainer.class.getName());
        p.setProperty(EJBContainer.MODULES, createWar());
        try {
            EJBContainer container = EJBContainer.createEJBContainer(p);
            assertNotNull(container);
            assertNotNull(container.getContext());
            container.close();
        } finally {
            new File(p.getProperty(EJBContainer.MODULES)).delete();
        }
    }

    private String createWar() throws IOException {
        File file = new File(System.getProperty("java.io.tmpdir") + "/tomee-" + Math.random());
        file.mkdirs();
        write("ok=true", new File(file, "index.html"));
        write("<beans />", new File(file, "META-INF/beans.xml"));
        return file.getAbsolutePath();
    }

    private static void write(String content, File file) throws IOException {
        file.getParentFile().mkdirs();
        FileWriter index = new FileWriter(file);
        index.write(content);
        index.close();
    }
}
