Title: REST Example with Application

*Help us document this example! Source available in [svn](http://svn.apache.org/repos/asf/openejb/trunk/openejb/examples/rest-example-with-application) or [git](https://github.com/apache/openejb/tree/trunk/openejb/examples/rest-example-with-application). Open a [JIRA](https://issues.apache.org/jira/browse/TOMEE) with patch or pull request*

## ApplicationConfig

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

## SimpleRESTEJB

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
        @GET
        public String ejb() {
            return "ejb ok @ " + new Date().toString();
        }
    }

## SimpleRESTPojo

    import javax.ws.rs.GET;
    import javax.ws.rs.Path;
    import java.util.Date;

    /**
     * @author rmannibucau
     */
    @Path("/pojo")
    public class SimpleRESTPojo {
        @GET
        public String pojo() {
            return "pojo ok @ " + new Date().toString();
        }
    }

## web.xml

    <web-app xmlns="http://java.sun.com/xml/ns/javaee"
             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             xsi:schemaLocation="http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/web-app_2_5.xsd"
             metadata-complete="false"
             version="2.5">
    
      <display-name>OpenEJB REST Example</display-name>
    </web-app>
    
