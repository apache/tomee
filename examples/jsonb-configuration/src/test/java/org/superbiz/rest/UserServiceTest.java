package org.superbiz.rest;

import java.io.IOException;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;


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


		String assertDate = LocalDate.of(2019, Month.JANUARY, 1)
				.format(DateTimeFormatter.ofPattern("yyyy - MM - dd"));

		System.out.println(assertDate);
		// test withDateFormat("yyyy - MM - dd")
		Assert.assertTrue(message.contains(assertDate));
		// test withFormatting(true)
		Assert.assertTrue(message.contains("\n"));
	}

}
