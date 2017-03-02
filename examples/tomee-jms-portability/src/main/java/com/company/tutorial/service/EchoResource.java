package com.company.tutorial.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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