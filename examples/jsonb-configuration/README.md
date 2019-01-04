index-group=JSON-B
type=page
status=published
~~~~~~

This examples shows how to customize jsonb for a JAX-RS Application. JSONB is the new javaee-api:8.0 standard for json serialization/deserialization. Few annotations are needed and JsonbConfig offers many configurations.

## Run and test Endpoint

the application can be run with 'mvn clean install tomee:run' if port 8080 is available you can invoke the following endpoint: (GET) http://localhost:8080/jsonb-configuration/api/users that should respond with the following json:
[
  {
    "Id":1,
    "Name":"user 1",
    "Registration":"2018 - 12 - 28"
  },
  {
    "Id":2,
    "Name":"user 2",
    "Registration":"2018 - 12 - 28"
  }
]

## @ApplicationPath

JAXRS entry point class, as follows jaxrs will load all the annotated @Path classes and methods without specifying them.

    import javax.ws.rs.ApplicationPath;
	import javax.ws.rs.core.Application;
	
	@ApplicationPath("api")
	public class JAXRSApplication extends Application {
	
	}

## @Path Rest resource

Simple jaxrs class with a GET endpoint

    import java.util.ArrayList;
	import java.util.List;
	
	import javax.ejb.Stateless;
	import javax.ws.rs.Consumes;
	import javax.ws.rs.GET;
	import javax.ws.rs.Path;
	import javax.ws.rs.Produces;
	import javax.ws.rs.core.MediaType;
	
	import org.superbiz.model.User;
	
	@Path("users")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Stateless
	public class UserService {
	
		@GET
		public List<User> users() {
			List<User> users = new ArrayList<>();
			User user1 = new User(1, "user 1");
			User user2 = new User(2, "user 2");
			users.add(user1);
			users.add(user2);
	
			return users;
		}
	}

## JSONB Configuration

Implementing ContextResolver<> you can customize jaxrs defaults, in this example we are going to customize JSONB serialization/deserialization

    import javax.json.bind.Jsonb;
	import javax.json.bind.JsonbBuilder;
	import javax.json.bind.JsonbConfig;
	import javax.json.bind.config.PropertyNamingStrategy;
	import javax.ws.rs.ext.ContextResolver;
	import javax.ws.rs.ext.Provider;
	
	@Provider
	public class JSONBConfiguration implements ContextResolver<Jsonb> {
	
		private Jsonb jsonb;
	
		public JSONBConfiguration() {
			// jsonbConfig offers a lot of configurations.
			JsonbConfig config = new JsonbConfig().withFormatting(true)
					.withPropertyNamingStrategy(PropertyNamingStrategy.UPPER_CAMEL_CASE)
					.withDateFormat("yyyy - MM - dd", Locale.ENGLISH);
	
			jsonb = JsonbBuilder.create(config);
		}
	
		@Override
		public Jsonb getContext(Class<?> type) {
			return jsonb;
		}
	
	}
	
JsonbConfig offers many configurations.

## Accessing the rest endpoint

The test spin up an openejb webapp and invoke the users endpoint

    import java.io.IOException;
	import java.text.SimpleDateFormat;
	import java.util.Date;
	
	import org.apache.cxf.jaxrs.client.WebClient;
	import org.apache.openejb.jee.WebApp;
	import org.apache.openejb.junit.ApplicationComposer;
	import org.apache.openejb.testing.Classes;
	import org.apache.openejb.testing.EnableServices;
	import org.apache.openejb.testing.Module;
	import org.junit.Assert;
	import org.junit.Test;
	import org.junit.runner.RunWith;
	import org.superbiz.JAXRSApplication;
	import org.superbiz.JSONBConfiguration;
	
	@EnableServices(value = "jaxrs")
	@RunWith(ApplicationComposer.class)
	public class UserServiceTest {
	
		@Module
		@Classes({ UserService.class, JAXRSApplication.class, JSONBConfiguration.class })
		public WebApp app() {
			return new WebApp().contextRoot("test");
		}
	
		@Test
		public void get() throws IOException {
			final String message = WebClient.create("http://localhost:4204").path("/test/api/users").get(String.class);
			System.out.println(message);
	
			final SimpleDateFormat sdf = new SimpleDateFormat("yyyy - MM - dd");
	
			// test withDateFormat("yyyy - MM - dd")
			Assert.assertTrue(message.contains(sdf.format(new Date())));
			// test withFormatting(true)
			Assert.assertTrue(message.contains(System.getProperty("line.separator")));
		}
	
	}

# Running

Running the example can be done from maven with a simple 'mvn clean install' command run from the 'jsonb-configuration' directory.

When run you should see output similar to the following.

    -------------------------------------------------------
	 T E S T S
	-------------------------------------------------------
	Running org.superbiz.rest.UserServiceTest
	INFO - Created new singletonService org.apache.openejb.cdi.ThreadSingletonServiceImpl@7823a2f9
	INFO - Succeeded in installing singleton service
	INFO - Cannot find the configuration file [conf/openejb.xml].  Will attempt to create one for the beans deployed.
	INFO - Configuring Service(id=Default Security Service, type=SecurityService, provider-id=Default Security Service)
	INFO - Configuring Service(id=Default Transaction Manager, type=TransactionManager, provider-id=Default Transaction Manager)
	INFO - Creating TransactionManager(id=Default Transaction Manager)
	INFO - Creating SecurityService(id=Default Security Service)
	INFO - Initializing network services
	INFO - Creating ServerService(id=cxf-rs)
	INFO - Creating ServerService(id=httpejbd)
	INFO - Created ServicePool 'httpejbd' with (10) core threads, limited to (200) threads with a queue of (9)
	INFO - Initializing network services
	INFO -   ** Bound Services **
	INFO -   NAME                 IP              PORT  
	INFO -   httpejbd             127.0.0.1       4204  
	INFO - -------
	INFO - Ready!
	INFO - Configuring enterprise application: /home/federico/Documents/PRIVATO/Apache/tomee/examples/jsonb-configuration/UserServiceTest
	INFO - Auto-deploying ejb UserService: EjbDeployment(deployment-id=UserService)
	INFO - Configuring Service(id=Default Managed Container, type=Container, provider-id=Default Managed Container)
	INFO - Auto-creating a container for bean org.superbiz.rest.UserServiceTest: Container(type=MANAGED, id=Default Managed Container)
	INFO - Creating Container(id=Default Managed Container)
	INFO - Using directory /tmp for stateful session passivation
	INFO - Configuring Service(id=Default Stateless Container, type=Container, provider-id=Default Stateless Container)
	INFO - Auto-creating a container for bean UserService: Container(type=STATELESS, id=Default Stateless Container)
	INFO - Creating Container(id=Default Stateless Container)
	INFO - Enterprise application "/home/federico/Documents/PRIVATO/Apache/tomee/examples/jsonb-configuration/UserServiceTest" loaded.
	INFO - Creating dedicated application classloader for UserServiceTest
	INFO - Assembling app: /home/federico/Documents/PRIVATO/Apache/tomee/examples/jsonb-configuration/UserServiceTest
	INFO - Jndi(name=UserServiceLocalBean) --> Ejb(deployment-id=UserService)
	INFO - Jndi(name=global/test/UserService!org.superbiz.rest.UserService) --> Ejb(deployment-id=UserService)
	INFO - Jndi(name=global/test/UserService) --> Ejb(deployment-id=UserService)
	INFO - Created Ejb(deployment-id=UserService, ejb-name=UserService, container=Default Stateless Container)
	INFO - Started Ejb(deployment-id=UserService, ejb-name=UserService, container=Default Stateless Container)
	INFO - Using readers:
	INFO -      org.apache.cxf.jaxrs.provider.PrimitiveTextProvider@2f94c4db
	INFO -      org.apache.cxf.jaxrs.provider.FormEncodingProvider@6b5966e1
	INFO -      org.apache.cxf.jaxrs.provider.MultipartProvider@65e61854
	INFO -      org.apache.cxf.jaxrs.provider.SourceProvider@1568159
	INFO -      org.apache.cxf.jaxrs.provider.JAXBElementTypedProvider@4fcee388
	INFO -      org.apache.cxf.jaxrs.provider.JAXBElementProvider@6f80fafe
	INFO -      org.apache.openejb.server.cxf.rs.johnzon.TomEEJsonbProvider@63cd604c
	INFO -      org.apache.openejb.server.cxf.rs.johnzon.TomEEJsonpProvider@593e824f
	INFO -      org.apache.cxf.jaxrs.provider.StringTextProvider@72ccd81a
	INFO -      org.apache.cxf.jaxrs.provider.BinaryDataProvider@6d8792db
	INFO -      org.apache.cxf.jaxrs.provider.DataSourceProvider@64bc21ac
	INFO - Using writers:
	INFO -      org.apache.johnzon.jaxrs.WadlDocumentMessageBodyWriter@493dfb8e
	INFO -      org.apache.cxf.jaxrs.nio.NioMessageBodyWriter@5d25e6bb
	INFO -      org.apache.cxf.jaxrs.provider.StringTextProvider@72ccd81a
	INFO -      org.apache.cxf.jaxrs.provider.JAXBElementTypedProvider@4fcee388
	INFO -      org.apache.cxf.jaxrs.provider.PrimitiveTextProvider@2f94c4db
	INFO -      org.apache.cxf.jaxrs.provider.FormEncodingProvider@6b5966e1
	INFO -      org.apache.cxf.jaxrs.provider.MultipartProvider@65e61854
	INFO -      org.apache.cxf.jaxrs.provider.SourceProvider@1568159
	INFO -      org.apache.cxf.jaxrs.provider.JAXBElementProvider@6f80fafe
	INFO -      org.apache.openejb.server.cxf.rs.johnzon.TomEEJsonbProvider@63cd604c
	INFO -      org.apache.openejb.server.cxf.rs.johnzon.TomEEJsonpProvider@593e824f
	INFO -      org.apache.cxf.jaxrs.provider.BinaryDataProvider@6d8792db
	INFO -      org.apache.cxf.jaxrs.provider.DataSourceProvider@64bc21ac
	INFO - Using exception mappers:
	INFO -      org.apache.cxf.jaxrs.impl.WebApplicationExceptionMapper@361c294e
	INFO -      org.apache.openejb.server.cxf.rs.EJBExceptionMapper@6fff253c
	INFO -      org.apache.cxf.jaxrs.validation.ValidationExceptionMapper@7859e786
	INFO -      org.apache.openejb.server.cxf.rs.CxfRsHttpListener$CxfResponseValidationExceptionMapper@285d851a
	INFO - REST Application: http://127.0.0.1:4204/test/api       -> org.superbiz.JAXRSApplication@5af28b27
	INFO -      Service URI: http://127.0.0.1:4204/test/api/users ->  EJB org.superbiz.rest.UserService
	INFO -               GET http://127.0.0.1:4204/test/api/users ->      List<User> users()
	INFO - Deployed Application(path=/home/federico/Documents/PRIVATO/Apache/tomee/examples/jsonb-configuration/UserServiceTest)
	[
	  {
	    "Id":1,
	    "Name":"user 1",
	    "Registration":"2018 - 12 - 28"
	  },
	  {
	    "Id":2,
	    "Name":"user 2",
	    "Registration":"2018 - 12 - 28"
	  }
	]
	INFO - Undeploying app: /home/federico/Documents/PRIVATO/Apache/tomee/examples/jsonb-configuration/UserServiceTest
	INFO - Stopping network services
	INFO - Stopping server services
	Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 2.203 sec
	
	Results :
	
	Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
	    
## Inside the jar

javaee-api:8.0 brings in all the dependencies needed to spin up a working REST application. 

If we look at the jar built by maven, we'll see the application itself is quite small:

    $ jar tvf target/jsonb-configuration-8.0.0-SNAPSHOT.war 
	     0 Fri Dec 28 19:36:10 CET 2018 META-INF/
	   134 Fri Dec 28 19:36:08 CET 2018 META-INF/MANIFEST.MF
	     0 Fri Dec 28 19:36:08 CET 2018 WEB-INF/
	     0 Fri Dec 28 19:36:08 CET 2018 WEB-INF/classes/
	     0 Fri Dec 28 19:36:08 CET 2018 WEB-INF/classes/org/
	     0 Fri Dec 28 19:36:08 CET 2018 WEB-INF/classes/org/superbiz/
	     0 Fri Dec 28 19:36:08 CET 2018 WEB-INF/classes/org/superbiz/model/
	     0 Fri Dec 28 19:36:08 CET 2018 WEB-INF/classes/org/superbiz/rest/
	  1165 Fri Dec 28 19:36:06 CET 2018 WEB-INF/classes/org/superbiz/model/User.class
	   402 Fri Dec 28 19:36:06 CET 2018 WEB-INF/classes/org/superbiz/JAXRSApplication.class
	  1194 Fri Dec 28 19:36:06 CET 2018 WEB-INF/classes/org/superbiz/rest/UserService.class
	  1701 Fri Dec 28 19:36:06 CET 2018 WEB-INF/classes/org/superbiz/JSONBConfiguration.class
	  1224 Fri Dec 28 18:28:32 CET 2018 WEB-INF/web.xml
	     0 Fri Dec 28 19:36:10 CET 2018 META-INF/maven/
	     0 Fri Dec 28 19:36:10 CET 2018 META-INF/maven/org.superbiz/
	     0 Fri Dec 28 19:36:10 CET 2018 META-INF/maven/org.superbiz/jsonb-configuration/
	  1791 Fri Dec 28 19:10:44 CET 2018 META-INF/maven/org.superbiz/jsonb-configuration/pom.xml
	   128 Fri Dec 28 19:36:08 CET 2018 META-INF/maven/org.superbiz/jsonb-configuration/pom.properties

This single jar could be deployed any any compliant Java EE implementation.  In TomEE you'd simply place it in the `tomee.home/webapps/` directory.