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
package org.superbiz.filterexample;

import java.util.Enumeration;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Invocation.Builder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/{testendpoint}")
public class ExampleServlet {
	
	@GET
	public String method(@Context HttpServletRequest request, @PathParam("testendpoint") String path) {
		StringBuilder sb = new StringBuilder("<html><head><title>JAX-RS Filter Example</title></head>");
		
		sb.append("<body><h2>HTTP Info</h2><div>Servlet HTTP Info</div><div>path:").append(path);
		
		sb.append("</div><div>IP:").append(request.getRemoteAddr());
		sb.append("</div><div>Host:").append(request.getRemoteHost());
		sb.append("</div><div>Port:").append(request.getRemotePort());
		sb.append("</div><div>URI:").append(request.getRequestURI());
		
		sb.append("</div><h3>Request Attrs (not necessarily HTTP, but are part of the API exposing it)</h3>");
		Enumeration<String> set = request.getAttributeNames();
		while (set.hasMoreElements()) {
			String s = set.nextElement();
			sb.append("<p>").append(s).append(":")
				.append(request.getAttribute(s))
				.append("</p>");
		}
		
		sb.append("<h3> Headers (if any) </h3>");
		Enumeration<String> headers = request.getHeaderNames();
		while (headers.hasMoreElements()) {
			String s = headers.nextElement();
			sb.append("<p>").append(s).append(":")
				.append(request.getHeader(s)).append("</p>");
		}
		
		Enumeration<String> queryParams = request.getParameterNames();
		sb.append("<h3>Query Params</h3>");
		while (queryParams.hasMoreElements()) {
			String qp = queryParams.nextElement();
			sb.append("<p>").append(qp).append(":");
				for (String s : request.getParameterValues(qp)) {
					sb.append(s).append(",");
				}
				sb.append("</p>");
		}
		
		return sb.append("</div></html>").toString();
	}
	
}
