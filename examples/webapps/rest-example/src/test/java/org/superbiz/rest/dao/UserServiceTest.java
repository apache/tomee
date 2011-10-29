package org.superbiz.rest.dao;

import org.apache.commons.io.FileUtils;
import org.apache.cxf.jaxrs.client.JAXRSClientFactory;
import org.apache.tomee.embedded.EmbeddedTomEEContainer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.superbiz.rest.model.User;

import javax.ejb.embeddable.EJBContainer;
import javax.naming.NamingException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import java.io.File;
import java.io.IOException;
import java.util.Properties;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

/**
 * @author rmannibucau
 */
public class UserServiceTest {

    private static EJBContainer container;
    private static File webApp;

    @BeforeClass
    public static void start() throws IOException {
        webApp = createWebApp();
        Properties p = new Properties();
        p.setProperty(EJBContainer.APP_NAME, "rest-example");
        p.setProperty(EJBContainer.PROVIDER, "tomee-embedded"); // need web feature
        p.setProperty(EJBContainer.MODULES, webApp.getAbsolutePath());
        p.setProperty(EmbeddedTomEEContainer.TOMEE_EJBCONTAINER_HTTP_PORT, "-1"); // random port
        container = EJBContainer.createEJBContainer(p);
    }

    @AfterClass
    public static void stop() {
        if (container != null) {
            container.close();
        }
        if (webApp != null) {
            try {
                FileUtils.forceDelete(webApp);
            } catch (IOException e) {
                FileUtils.deleteQuietly(webApp);
            }
        }
    }

    @Test
    public void create() throws NamingException {
        UserDAO dao = (UserDAO) container.getContext().lookup("java:global/rest-example/UserDAO");
        User user = dao.create("foo", "dummy", "foo@dummy.org");
        assertNotNull(dao.find(user.getId()));

        String uri = "http://127.0.0.1:" + System.getProperty(EmbeddedTomEEContainer.TOMEE_EJBCONTAINER_HTTP_PORT) + "/" + webApp.getName();
        UserServiceClientAPI client = JAXRSClientFactory.create(uri, UserServiceClientAPI.class);
        User retrievedUser = client.show(user.getId());
        assertNotNull(retrievedUser);
        assertEquals("foo", retrievedUser.getFullname());
        assertEquals("dummy", retrievedUser.getPassword());
        assertEquals("foo@dummy.org", retrievedUser.getEmail());
    }

    private static File createWebApp() throws IOException {
        File file = new File(System.getProperty("java.io.tmpdir") + "/tomee-" + Math.random());
        if (!file.mkdirs() && !file.exists()) {
            throw new RuntimeException("can't create " + file.getAbsolutePath());
        }

        FileUtils.copyDirectory(new File("target/classes"), new File(file, "WEB-INF/classes"));

        return file;
    }

    /**
     * a simple copy of the unique method i want to use from my service.
     * It allows to use cxf proxy to call remotely our rest service.
     * Any other way to do it is good.
     */
    @Path("/api/user")
    @Produces({"text/xml", "application/json"})
    public static interface UserServiceClientAPI {

        @Path("/show/{id}")
        @GET
        User show(@PathParam("id") long id);
    }
}
