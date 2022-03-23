/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.superbiz.rest;

import java.io.IOException;

import jakarta.ws.rs.core.MediaType;

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
