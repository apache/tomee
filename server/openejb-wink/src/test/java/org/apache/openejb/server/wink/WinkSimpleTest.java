package org.apache.openejb.server.wink;

import javax.ws.rs.PathParam;
import org.apache.openejb.OpenEjbContainer;
import org.apache.openejb.loader.IO;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ejb.Stateless;
import javax.ejb.embeddable.EJBContainer;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

import static junit.framework.Assert.assertEquals;

public class WinkSimpleTest {
    private static EJBContainer container;

    @BeforeClass
    public static void start() throws Exception {
        Properties properties = new Properties();
        properties.setProperty(OpenEjbContainer.OPENEJB_EMBEDDED_REMOTABLE, "true");
        container = EJBContainer.createEJBContainer(properties);
    }

    @AfterClass
    public static void close() throws Exception {
        if (container != null) {
            container.close();
        }
    }

    @Test public void rest() throws IOException {
        String response = IO.slurp(new URL("http://localhost:4204/openejb-wink/ejb/normal"));
        assertEquals("ok", response);
    }

    @Test public void rest2() throws IOException {
        String response = IO.slurp(new URL("http://localhost:4204/openejb-wink/ejb2/ok2"));
        assertEquals("ok2", response);
    }

    @Test public void foo() throws IOException {
        String response = IO.slurp(new URL("http://localhost:4204/openejb-wink/ejb2/foo/ok"));
        assertEquals("_ok_", response);
    }

    @Stateless
    @Path("/ejb")
    public static class RESTIsCool {
        @Path("/normal") @GET
        public String normal() {
            return "ok";
        }
    }

    @Stateless
    @Path("/ejb2")
    public static class RESTIsCool2 {
        @Path("/ok2") @GET
        public String normal() {
            return "ok2";
        }

        @Path("/foo/{bar}") @GET
        public String foo(@PathParam("bar") final String bar) {
            return "_" + bar + "_";
        }
    }
}
