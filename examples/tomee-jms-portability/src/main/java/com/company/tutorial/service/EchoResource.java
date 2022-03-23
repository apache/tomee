/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.company.tutorial.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Path("/echo")
public class EchoResource {
	final static Logger logger = LoggerFactory.getLogger(EchoResource.class);
	@PUT
	@Produces({ MediaType.TEXT_PLAIN })
	@Path("/")
	public Response echoPut(@Context HttpServletRequest req) throws IOException {
		String body = read(req.getInputStream());
		logger.debug("Received PUT/POST request: " + body);
		return Response.ok(body).build();
	}
	
	@POST
	@Produces({ MediaType.TEXT_PLAIN })
	@Path("/")
	public Response echoPost(@Context HttpServletRequest req) throws IOException {
		return echoPut(req);
	}
	
	@GET
	@Produces({ MediaType.TEXT_PLAIN })
	@Path("/")
	public Response echoGet(@Context HttpServletRequest req) {
		logger.debug("Received GET request.");
		return Response.ok().build();
	}
	
    public static String read(InputStream input) throws IOException {
        try (BufferedReader buffer = new BufferedReader(new InputStreamReader(input))) {
            return buffer.lines().collect(Collectors.joining("\n"));
        }
    }
}