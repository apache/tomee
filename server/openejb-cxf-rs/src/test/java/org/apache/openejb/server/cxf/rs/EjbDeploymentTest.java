package org.apache.openejb.server.cxf.rs;

import java.util.Properties;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.embeddable.EJBContainer;
import javax.enterprise.inject.Default;
import javax.naming.Context;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.openejb.OpenEjbContainer;
import org.apache.openejb.server.cxf.rs.beans.SimpleEJB;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

/**
 * @author Romain Manni-Bucau
 */
public class EjbDeploymentTest {
    private static Context context;
    private static RESTIsCool service;

    @BeforeClass public static void start() throws Exception {
        Properties properties = new Properties();
        properties.setProperty(OpenEjbContainer.OPENEJB_EMBEDDED_REMOTABLE, "true");
        context = EJBContainer.createEJBContainer(properties).getContext();
        service = (RESTIsCool) context.lookup("java:/global/openejb-cxf-rs/RESTIsCool");
    }

    @AfterClass public static void close() throws Exception {
        if (context != null) {
            context.close();
        }
    }

    @Test public void deploy() {
        // service works
        assertNotNull(service);
        assertEquals("ok", service.ok(true));

        // rest invocation works
        String response = WebClient.create("http://localhost:4204").path("/ejb/rest").get(String.class);
        assertEquals("ok", response);
    }

    @Path("/ejb")
    @Stateless
    public static class RESTIsCool {
        @javax.ws.rs.core.Context private UriInfo uriInfo;
        @EJB private SimpleEJB simpleEJB;

        @Path("/rest") @GET public String ok(@QueryParam("force") @DefaultValue("false") boolean force) {
            /*if (!(uriInfo != null || force)) {
                return "ko";
            }*/
            return simpleEJB.ok();
        }
    }
}
