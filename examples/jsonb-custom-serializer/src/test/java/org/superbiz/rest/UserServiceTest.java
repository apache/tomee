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
import java.net.URL;

import jakarta.ws.rs.core.MediaType;

import org.apache.cxf.jaxrs.client.WebClient;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.superbiz.AddressSerializer;
import org.superbiz.JAXRSApplication;
import org.superbiz.JSONBConfiguration;
import org.superbiz.UserDeserializer;
import org.superbiz.model.Address;
import org.superbiz.model.User;

@RunWith(Arquillian.class)
public class UserServiceTest {

	@ArquillianResource
	private URL base;

	@Deployment(testable = false)
	public static WebArchive app() {
		return ShrinkWrap.create(WebArchive.class)
				.addClasses(UserService.class, JAXRSApplication.class, JSONBConfiguration.class,
						AddressSerializer.class, UserDeserializer.class, Address.class, User.class);
	}

	@Test
	public void get() throws IOException {
		final String message = WebClient.create(base.toExternalForm()).path("api/users").get(String.class);

		Assert.assertTrue(message.contains("modified - addr1"));
	}

	@Test
	public void post() throws IOException {
		final String inputJson = "{ \"id\": 1, \"name\": \"user1\", \"extra\": \"extraField\"}";
		final User responseUser = WebClient.create(base.toExternalForm()).path("api/users")
				.type(MediaType.APPLICATION_JSON).post(inputJson, User.class);

		Assert.assertTrue(!responseUser.getName().equals("user1"));
		Assert.assertTrue(responseUser.getName().equals("user1" + "extraField"));
	}

}
