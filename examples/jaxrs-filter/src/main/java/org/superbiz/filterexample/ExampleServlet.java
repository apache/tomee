package org.superbiz.filterexample;

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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
