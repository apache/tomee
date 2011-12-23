package org.apache.tomee.embedded;

import org.apache.commons.io.FileUtils;
import org.apache.openejb.OpenEjbContainer;
import org.apache.openejb.util.IOUtils;
import org.junit.Test;

import javax.ejb.embeddable.EJBContainer;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

public class EmbeddedTomEEContainerTest {
    @Test(expected = OpenEjbContainer.NoModulesFoundException.class) public void noModule() {
        Properties p = new Properties();
        p.setProperty(EJBContainer.APP_NAME, "test");
        p.setProperty(EJBContainer.PROVIDER, EmbeddedTomEEContainer.class.getName());
        EJBContainer.createEJBContainer(p);
    }

    @Test public void containerTest() throws Exception {
        File war = createWar();
        Properties p = new Properties();
        p.setProperty(EJBContainer.APP_NAME, "test");
        p.setProperty(EJBContainer.PROVIDER, EmbeddedTomEEContainer.class.getName());
        p.put(EJBContainer.MODULES, war.getAbsolutePath());
        p.setProperty(EmbeddedTomEEContainer.TOMEE_EJBCONTAINER_HTTP_PORT, "-1");
        try {
            EJBContainer container = EJBContainer.createEJBContainer(p);
            assertNotNull(container);
            assertNotNull(container.getContext());
            URL url = new URL("http://127.0.0.1:" + System.getProperty(EmbeddedTomEEContainer.TOMEE_EJBCONTAINER_HTTP_PORT) + "/" + war.getName() + "/index.html");
            assertEquals("true", IOUtils.readProperties(url).getProperty("ok"));
            container.close();
        } finally {
            try {
                FileUtils.forceDelete(war);
            } catch (IOException e) {
                FileUtils.deleteQuietly(war);
            }
        }
    }

    private File createWar() throws IOException {
        File file = new File(System.getProperty("java.io.tmpdir") + "/tomee-" + Math.random());
        if (!file.mkdirs() && !file.exists()) {
            throw new RuntimeException("can't create " + file.getAbsolutePath());
        }

        write("ok=true", new File(file, "index.html"));
        write("<beans />", new File(file, "WEB-INF/classes/META-INF/beans.xml"));
        return file;
    }

    private static void write(String content, File file) throws IOException {
        if (!file.getParentFile().mkdirs() && !file.getParentFile().exists()) {
            throw new RuntimeException("can't create " + file.getParent());
        }

        FileWriter index = new FileWriter(file);
        index.write(content);
        index.close();
    }
}
