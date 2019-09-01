package org.superbiz.rest;

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
