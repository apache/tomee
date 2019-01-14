index-group=JSON-B
type=page
status=published
~~~~~~

This examples shows how to customize objects serialization/deserialization with jsonb for a JAX-RS Application.

## Run and test Endpoint

the application can be run with 'mvn clean install tomee:run' if port 8080 is available you can invoke the following endpoint: (GET) http://localhost:8080/jsonb-custom-serializer/api/users that should respond with the following json:

	[
	  {
	    "address":{
	      "addr":"modified - addr1"
	    },
	    "id":1,
	    "name":"user 1",
	    "registration":"2018-12-29T18:13:40.028"
	  },
	  {
	    "address":{
	      "addr":"modified - addr2"
	    },
	    "id":2,
	    "name":"user 2",
	    "registration":"2018-12-29T18:13:40.028"
	  }
	]

and the endpoint: (POST) http://localhost:8080/jsonb-custom-serializer/api/users with a body like:
	
	{ 
		"id": 1, 
		"name": "name1", 
		"extra": "extra1" 
	}
	
which respond with the following json:

	{ 
		"id": 1, 
		"name": "name1extra1", 
		"registration": "Sat Dec 29 19:48:05 CET 2018",
		"address": null 
	}

## @ApplicationPath

JAXRS entry point class, as follows jaxrs will load all the annotated @Path classes and methods without specifying them.

    import javax.ws.rs.ApplicationPath;
	import javax.ws.rs.core.Application;
	
	@ApplicationPath("api")
	public class JAXRSApplication extends Application {
	
	}

## @Path Rest resource

Simple jaxrs class with a GET and a POST endpoint

	import java.util.ArrayList;
	import java.util.List;
	
	import javax.ejb.Stateless;
	import javax.ws.rs.Consumes;
	import javax.ws.rs.GET;
	import javax.ws.rs.POST;
	import javax.ws.rs.Path;
	import javax.ws.rs.Produces;
	import javax.ws.rs.core.MediaType;
	
	import org.superbiz.model.Address;
	import org.superbiz.model.User;
	
	@Path("users")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Stateless
	public class UserService {
	
		@GET
		public List<User> users() {
			List<User> users = new ArrayList<>();
			User user1 = new User(1, "user 1", new Address("addr1"));
			User user2 = new User(2, "user 2", new Address("addr2"));
			users.add(user1);
			users.add(user2);
	
			return users;
		}
	
		@POST
		public User addUser(User u) {
			// Just to show the deserialization
			return u;
		}
	}


## JSONB Configuration

Implementing ContextResolver<> you can customize jaxrs defaults, in this example we are going to customize JSONB serialization for all objects of type Address, defined in AddressSerializer class

	import javax.json.bind.Jsonb;
	import javax.json.bind.JsonbBuilder;
	import javax.json.bind.JsonbConfig;
	import javax.ws.rs.ext.ContextResolver;
	import javax.ws.rs.ext.Provider;
	
	@Provider
	public class JSONBConfiguration implements ContextResolver<Jsonb> {
	
		private Jsonb jsonb;
	
		public JSONBConfiguration() {
			JsonbConfig config = new JsonbConfig().withFormatting(true).withSerializers(new AddressSerializer());
	
			jsonb = JsonbBuilder.create(config);
		}
	
		@Override
		public Jsonb getContext(Class<?> type) {
			return jsonb;
		}
	
	}
	
## Address serializer

Simple Object serializer class that add 'modified' during serialization

	import javax.json.bind.serializer.JsonbSerializer;
	import javax.json.bind.serializer.SerializationContext;
	import javax.json.stream.JsonGenerator;
	
	import org.superbiz.model.Address;
	
	public class AddressSerializer implements JsonbSerializer<Address> {
	
		@Override
		public void serialize(Address obj, JsonGenerator generator, SerializationContext ctx) {
			if (obj != null) {
				obj.setAddr("modified - " + obj.getAddr());
				ctx.serialize(obj, generator);
			}
	
		}
	
	}


## User Deserializer

Create an object from a json

	import java.lang.reflect.Type;
	
	import javax.json.JsonObject;
	import javax.json.bind.serializer.DeserializationContext;
	import javax.json.bind.serializer.JsonbDeserializer;
	import javax.json.stream.JsonParser;
	
	import org.superbiz.model.User;
	
	public class UserDeserializer implements JsonbDeserializer<User> {
	
		@Override
		public User deserialize(JsonParser parser, DeserializationContext ctx, Type rtType) {
			JsonObject jo = parser.getObject();
			String name = jo.get("name").toString().replace("\"", "");
			if (jo.get("extra") != null) {
				name = name + jo.get("extra").toString().replace("\"", "");
			}
			User u = new User(Integer.parseInt(jo.get("id").toString()), name, null);
	
			return u;
		}
	
	}
	
## Using the deserializer @JsonbTypeDeserializer @JsonbTypeSerializer

With the annotation @JsonbTypeDeserializer or @JsonbTypeSerializer you can notify jsonb to use the custom deserializer 

	import java.util.Date;
	
	import javax.json.bind.annotation.JsonbTypeDeserializer;
	
	import org.superbiz.UserDeserializer;
	
	@JsonbTypeDeserializer(UserDeserializer.class)
	public class User {
	
		private Integer id;
		private String name;
		private Date registration = new Date();
		private Address address;
	
		public User(Integer id, String name, Address address) {
			super();
			this.id = id;
			this.name = name;
			this.address = address;
		}
	
		public User() {
			super();
		}
	
		// ... GET/SET
	
	} 

## Accessing the rest endpoint

The test spin up an openejb webapp and invoke the users endpoint

	import java.io.IOException;
	
	import javax.ws.rs.core.MediaType;
	
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
	import org.superbiz.model.User;
	
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
	
			Assert.assertTrue(message.contains("modified - addr1"));
		}
	
		@Test
		public void post() throws IOException {
			final String inputJson = "{ \"id\": 1, \"name\": \"user1\", \"extra\": \"extraField\"}";
			final User responseUser = WebClient.create("http://localhost:4204").path("/test/api/users")
					.type(MediaType.APPLICATION_JSON).post(inputJson, User.class);
	
			Assert.assertTrue(!responseUser.getName().equals("user1"));
			Assert.assertTrue(responseUser.getName().equals("user1" + "extraField"));
		}
	
	}


# Running

Running the example can be done from maven with a simple 'mvn clean install' command run from the 'jsonb-custom-serializer' directory.

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
	INFO - Configuring enterprise application: /home/federico/Documents/PRIVATO/Apache/tomee/examples/jsonb-custom-serializer/UserServiceTest
	INFO - Auto-deploying ejb UserService: EjbDeployment(deployment-id=UserService)
	INFO - Configuring Service(id=Default Managed Container, type=Container, provider-id=Default Managed Container)
	INFO - Auto-creating a container for bean org.superbiz.rest.UserServiceTest: Container(type=MANAGED, id=Default Managed Container)
	INFO - Creating Container(id=Default Managed Container)
	INFO - Using directory /tmp for stateful session passivation
	INFO - Configuring Service(id=Default Stateless Container, type=Container, provider-id=Default Stateless Container)
	INFO - Auto-creating a container for bean UserService: Container(type=STATELESS, id=Default Stateless Container)
	INFO - Creating Container(id=Default Stateless Container)
	INFO - Enterprise application "/home/federico/Documents/PRIVATO/Apache/tomee/examples/jsonb-custom-serializer/UserServiceTest" loaded.
	INFO - Creating dedicated application classloader for UserServiceTest
	INFO - Assembling app: /home/federico/Documents/PRIVATO/Apache/tomee/examples/jsonb-custom-serializer/UserServiceTest
	INFO - Jndi(name=UserServiceLocalBean) --> Ejb(deployment-id=UserService)
	INFO - Jndi(name=global/test/UserService!org.superbiz.rest.UserService) --> Ejb(deployment-id=UserService)
	INFO - Jndi(name=global/test/UserService) --> Ejb(deployment-id=UserService)
	INFO - Created Ejb(deployment-id=UserService, ejb-name=UserService, container=Default Stateless Container)
	INFO - Started Ejb(deployment-id=UserService, ejb-name=UserService, container=Default Stateless Container)
	INFO - Using readers:
	INFO -      org.apache.cxf.jaxrs.provider.PrimitiveTextProvider@6a1d204a
	INFO -      org.apache.cxf.jaxrs.provider.FormEncodingProvider@28a0fd6c
	INFO -      org.apache.cxf.jaxrs.provider.MultipartProvider@2b62442c
	INFO -      org.apache.cxf.jaxrs.provider.SourceProvider@66629f63
	INFO -      org.apache.cxf.jaxrs.provider.JAXBElementTypedProvider@841e575
	INFO -      org.apache.cxf.jaxrs.provider.JAXBElementProvider@27a5328c
	INFO -      org.apache.openejb.server.cxf.rs.johnzon.TomEEJsonbProvider@5ab14cb9
	INFO -      org.apache.openejb.server.cxf.rs.johnzon.TomEEJsonpProvider@62dae245
	INFO -      org.apache.cxf.jaxrs.provider.StringTextProvider@4b6579e8
	INFO -      org.apache.cxf.jaxrs.provider.BinaryDataProvider@6fff253c
	INFO -      org.apache.cxf.jaxrs.provider.DataSourceProvider@6c6357f9
	INFO - Using writers:
	INFO -      org.apache.johnzon.jaxrs.WadlDocumentMessageBodyWriter@591e58fa
	INFO -      org.apache.cxf.jaxrs.nio.NioMessageBodyWriter@3954d008
	INFO -      org.apache.cxf.jaxrs.provider.StringTextProvider@4b6579e8
	INFO -      org.apache.cxf.jaxrs.provider.JAXBElementTypedProvider@841e575
	INFO -      org.apache.cxf.jaxrs.provider.PrimitiveTextProvider@6a1d204a
	INFO -      org.apache.cxf.jaxrs.provider.FormEncodingProvider@28a0fd6c
	INFO -      org.apache.cxf.jaxrs.provider.MultipartProvider@2b62442c
	INFO -      org.apache.cxf.jaxrs.provider.SourceProvider@66629f63
	INFO -      org.apache.cxf.jaxrs.provider.JAXBElementProvider@27a5328c
	INFO -      org.apache.openejb.server.cxf.rs.johnzon.TomEEJsonbProvider@5ab14cb9
	INFO -      org.apache.openejb.server.cxf.rs.johnzon.TomEEJsonpProvider@62dae245
	INFO -      org.apache.cxf.jaxrs.provider.BinaryDataProvider@6fff253c
	INFO -      org.apache.cxf.jaxrs.provider.DataSourceProvider@6c6357f9
	INFO - Using exception mappers:
	INFO -      org.apache.cxf.jaxrs.impl.WebApplicationExceptionMapper@403132fc
	INFO -      org.apache.openejb.server.cxf.rs.EJBExceptionMapper@32cb636e
	INFO -      org.apache.cxf.jaxrs.validation.ValidationExceptionMapper@71c5b236
	INFO -      org.apache.openejb.server.cxf.rs.CxfRsHttpListener$CxfResponseValidationExceptionMapper@2cab9998
	INFO - REST Application: http://127.0.0.1:4204/test/api       -> org.superbiz.JAXRSApplication@285d851a
	INFO -      Service URI: http://127.0.0.1:4204/test/api/users ->  EJB org.superbiz.rest.UserService
	INFO -               GET http://127.0.0.1:4204/test/api/users ->      List<User> users()
	INFO -              POST http://127.0.0.1:4204/test/api/users ->      User addUser(User)
	INFO - Deployed Application(path=/home/federico/Documents/PRIVATO/Apache/tomee/examples/jsonb-custom-serializer/UserServiceTest)
	INFO - Undeploying app: /home/federico/Documents/PRIVATO/Apache/tomee/examples/jsonb-custom-serializer/UserServiceTest
	INFO - Stopping network services
	INFO - Stopping server services
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
	INFO - Configuring enterprise application: /home/federico/Documents/PRIVATO/Apache/tomee/examples/jsonb-custom-serializer/UserServiceTest
	INFO - Auto-deploying ejb UserService: EjbDeployment(deployment-id=UserService)
	INFO - Configuring Service(id=Default Managed Container, type=Container, provider-id=Default Managed Container)
	INFO - Auto-creating a container for bean org.superbiz.rest.UserServiceTest: Container(type=MANAGED, id=Default Managed Container)
	INFO - Creating Container(id=Default Managed Container)
	INFO - Using directory /tmp for stateful session passivation
	INFO - Configuring Service(id=Default Stateless Container, type=Container, provider-id=Default Stateless Container)
	INFO - Auto-creating a container for bean UserService: Container(type=STATELESS, id=Default Stateless Container)
	INFO - Creating Container(id=Default Stateless Container)
	INFO - Enterprise application "/home/federico/Documents/PRIVATO/Apache/tomee/examples/jsonb-custom-serializer/UserServiceTest" loaded.
	INFO - Creating dedicated application classloader for UserServiceTest
	INFO - Assembling app: /home/federico/Documents/PRIVATO/Apache/tomee/examples/jsonb-custom-serializer/UserServiceTest
	INFO - Jndi(name=UserServiceLocalBean) --> Ejb(deployment-id=UserService)
	INFO - Jndi(name=global/test/UserService!org.superbiz.rest.UserService) --> Ejb(deployment-id=UserService)
	INFO - Jndi(name=global/test/UserService) --> Ejb(deployment-id=UserService)
	INFO - Created Ejb(deployment-id=UserService, ejb-name=UserService, container=Default Stateless Container)
	INFO - Started Ejb(deployment-id=UserService, ejb-name=UserService, container=Default Stateless Container)
	INFO - Using readers:
	INFO -      org.apache.cxf.jaxrs.provider.PrimitiveTextProvider@51a06cbe
	INFO -      org.apache.cxf.jaxrs.provider.FormEncodingProvider@6cc0bcf6
	INFO -      org.apache.cxf.jaxrs.provider.MultipartProvider@29539e36
	INFO -      org.apache.cxf.jaxrs.provider.SourceProvider@32f61a31
	INFO -      org.apache.cxf.jaxrs.provider.JAXBElementTypedProvider@f5c79a6
	INFO -      org.apache.cxf.jaxrs.provider.JAXBElementProvider@669253b7
	INFO -      org.apache.openejb.server.cxf.rs.johnzon.TomEEJsonbProvider@5ab14cb9
	INFO -      org.apache.openejb.server.cxf.rs.johnzon.TomEEJsonpProvider@62dae245
	INFO -      org.apache.cxf.jaxrs.provider.StringTextProvider@3dddbe65
	INFO -      org.apache.cxf.jaxrs.provider.BinaryDataProvider@49a64d82
	INFO -      org.apache.cxf.jaxrs.provider.DataSourceProvider@344561e0
	INFO - Using writers:
	INFO -      org.apache.johnzon.jaxrs.WadlDocumentMessageBodyWriter@66d23e4a
	INFO -      org.apache.cxf.jaxrs.nio.NioMessageBodyWriter@36ac8a63
	INFO -      org.apache.cxf.jaxrs.provider.StringTextProvider@3dddbe65
	INFO -      org.apache.cxf.jaxrs.provider.JAXBElementTypedProvider@f5c79a6
	INFO -      org.apache.cxf.jaxrs.provider.PrimitiveTextProvider@51a06cbe
	INFO -      org.apache.cxf.jaxrs.provider.FormEncodingProvider@6cc0bcf6
	INFO -      org.apache.cxf.jaxrs.provider.MultipartProvider@29539e36
	INFO -      org.apache.cxf.jaxrs.provider.SourceProvider@32f61a31
	INFO -      org.apache.cxf.jaxrs.provider.JAXBElementProvider@669253b7
	INFO -      org.apache.openejb.server.cxf.rs.johnzon.TomEEJsonbProvider@5ab14cb9
	INFO -      org.apache.openejb.server.cxf.rs.johnzon.TomEEJsonpProvider@62dae245
	INFO -      org.apache.cxf.jaxrs.provider.BinaryDataProvider@49a64d82
	INFO -      org.apache.cxf.jaxrs.provider.DataSourceProvider@344561e0
	INFO - Using exception mappers:
	INFO -      org.apache.cxf.jaxrs.impl.WebApplicationExceptionMapper@4d9d1b69
	INFO -      org.apache.openejb.server.cxf.rs.EJBExceptionMapper@5305c37d
	INFO -      org.apache.cxf.jaxrs.validation.ValidationExceptionMapper@52c8295b
	INFO -      org.apache.openejb.server.cxf.rs.CxfRsHttpListener$CxfResponseValidationExceptionMapper@251f7d26
	INFO - REST Application: http://127.0.0.1:4204/test/api       -> org.superbiz.JAXRSApplication@77b21474
	INFO -      Service URI: http://127.0.0.1:4204/test/api/users ->  EJB org.superbiz.rest.UserService
	INFO -               GET http://127.0.0.1:4204/test/api/users ->      List<User> users()
	INFO -              POST http://127.0.0.1:4204/test/api/users ->      User addUser(User)
	INFO - Deployed Application(path=/home/federico/Documents/PRIVATO/Apache/tomee/examples/jsonb-custom-serializer/UserServiceTest)
	INFO - Undeploying app: /home/federico/Documents/PRIVATO/Apache/tomee/examples/jsonb-custom-serializer/UserServiceTest
	INFO - Stopping network services
	INFO - Stopping server services
	Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 2.31 sec
	
	Results :
	
	Tests run: 2, Failures: 0, Errors: 0, Skipped: 0
	    
## Inside the jar

javaee-api:8.0 brings in all the dependencies needed to spin up a working REST application. 

If we look at the jar built by maven, we'll see the application itself is quite small:

    $ jar tvf target/jsonb-custom-serializer-8.0.0-SNAPSHOT.war 
	     0 Sat Dec 29 19:10:44 CET 2018 META-INF/
	   134 Sat Dec 29 19:10:42 CET 2018 META-INF/MANIFEST.MF
	     0 Sat Dec 29 19:10:42 CET 2018 WEB-INF/
	     0 Sat Dec 29 19:10:42 CET 2018 WEB-INF/classes/
	     0 Sat Dec 29 19:10:42 CET 2018 WEB-INF/classes/org/
	     0 Sat Dec 29 19:10:42 CET 2018 WEB-INF/classes/org/superbiz/
	     0 Sat Dec 29 19:10:42 CET 2018 WEB-INF/classes/org/superbiz/model/
	     0 Sat Dec 29 19:10:42 CET 2018 WEB-INF/classes/org/superbiz/rest/
	   790 Sat Dec 29 19:10:38 CET 2018 WEB-INF/classes/org/superbiz/model/Address.class
	  2093 Sat Dec 29 19:10:38 CET 2018 WEB-INF/classes/org/superbiz/model/User.class
	  2063 Sat Dec 29 19:10:38 CET 2018 WEB-INF/classes/org/superbiz/UserDeserializer.class
	   402 Sat Dec 29 19:10:38 CET 2018 WEB-INF/classes/org/superbiz/JAXRSApplication.class
	  1461 Sat Dec 29 19:10:38 CET 2018 WEB-INF/classes/org/superbiz/AddressSerializer.class
	  1498 Sat Dec 29 19:10:38 CET 2018 WEB-INF/classes/org/superbiz/rest/UserService.class
	  1549 Sat Dec 29 19:10:38 CET 2018 WEB-INF/classes/org/superbiz/JSONBConfiguration.class
	  1241 Sat Dec 29 17:52:48 CET 2018 WEB-INF/web.xml
	     0 Sat Dec 29 19:10:44 CET 2018 META-INF/maven/
	     0 Sat Dec 29 19:10:44 CET 2018 META-INF/maven/org.superbiz/
	     0 Sat Dec 29 19:10:44 CET 2018 META-INF/maven/org.superbiz/jsonb-custom-serializer/
	  1811 Sat Dec 29 17:53:36 CET 2018 META-INF/maven/org.superbiz/jsonb-custom-serializer/pom.xml
	   132 Sat Dec 29 19:10:42 CET 2018 META-INF/maven/org.superbiz/jsonb-custom-serializer/pom.properties

This single jar could be deployed any any compliant Java EE implementation.  In TomEE you'd simply place it in the `tomee.home/webapps/` directory.