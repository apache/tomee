package org.apache.openejb.arquillian.tests.jaxrs;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

@ApplicationPath("/app")
public class RSApplication extends Application {
    public Set<Object> getSingletons() {
        return new HashSet<Object>() {{
            add(new RSService());
        }};
    }

    @Path("/service")
    public static class RSService {
        @GET
        public String foo() {
            return "foo";
        }
        
    }
}
