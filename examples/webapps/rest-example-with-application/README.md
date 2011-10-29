    package org.superbiz.rest.application;
    
    import javax.ws.rs.ApplicationPath;
    import javax.ws.rs.core.Application;
    import java.util.Arrays;
    import java.util.HashSet;
    import java.util.Set;
    
    /**
     * @author rmannibucau
     */
    @ApplicationPath("/rest-prefix")
    public class ApplicationConfig extends Application {
        public Set<Class<?>> getClasses() {
            return new HashSet<Class<?>>(Arrays.asList(SimpleRESTPojo.class, SimpleRESTEJB.class));
        }
    }
    package org.superbiz.rest.application;
    
    import javax.ejb.Lock;
    import javax.ejb.LockType;
    import javax.ejb.Singleton;
    import javax.ws.rs.GET;
    import javax.ws.rs.Path;
    import java.util.Date;
    
    /**
     * @author rmannibucau
     */
    @Singleton
    @Lock(LockType.READ)
    @Path("/ejb")
    public class SimpleRESTEJB {
        @GET public String ejb() {
            return "ejb ok @ " + new Date().toString();
        }
    }
    package org.superbiz.rest.application;
    
    import javax.ws.rs.GET;
    import javax.ws.rs.Path;
    import java.util.Date;
    
    /**
     * @author rmannibucau
     */
    @Path("/pojo")
    public class SimpleRESTPojo {
        @GET public String pojo() {
            return "pojo ok @ " + new Date().toString();
        }
    }
