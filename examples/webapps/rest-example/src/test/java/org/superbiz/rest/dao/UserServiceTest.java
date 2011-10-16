package org.superbiz.rest.dao;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.tomee.embedded.EmbeddedTomEEContainer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.superbiz.rest.model.User;

import javax.ejb.embeddable.EJBContainer;
import javax.naming.NamingException;
import java.io.File;
import java.io.IOException;
import java.util.Properties;

import static junit.framework.Assert.assertNotNull;

/**
 * @author rmannibucau
 */
public class UserServiceTest {
    private static EJBContainer container;
    private static File webApp;

    @BeforeClass public static void start() throws IOException {
        webApp = createWebApp();
        Properties p = new Properties();
        p.setProperty(EJBContainer.APP_NAME, "test");
        p.setProperty(EJBContainer.PROVIDER, EmbeddedTomEEContainer.class.getName()); // need web feature
        p.setProperty(EJBContainer.MODULES, webApp.getAbsolutePath());
        p.setProperty(EmbeddedTomEEContainer.TOMEE_EJBCONTAINER_HTTP_PORT, "-1"); // random port
        container = EJBContainer.createEJBContainer(p);
    }

    @AfterClass public static void stop() {
        if (container != null) {
            container.close();
        }
        if (webApp != null) {
            if (!webApp.delete()) {
                webApp.deleteOnExit();
            }
        }
    }

    @Test public void create() throws NamingException {
        UserDAO dao = (UserDAO) container.getContext().lookup("java:global/" + webApp.getName() + "/UserDAO");
        User user = dao.create("foo", "dummy", "foo@dummy.org");
        assertNotNull(dao.find(user.getId()));
    }

    private static File createWebApp() throws IOException {
        File file = new File(System.getProperty("java.io.tmpdir") + "/tomee-" + Math.random());
        if (!file.mkdirs() && !file.exists()) {
            throw new RuntimeException("can't create " + file.getAbsolutePath());
        }

        FileUtils.copyDirectory(new File("target/classes"), new File(file, "WEB-INF/classes"), TrueFileFilter.INSTANCE);

        return file;
    }
}
