package org.superbiz.rest;

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
