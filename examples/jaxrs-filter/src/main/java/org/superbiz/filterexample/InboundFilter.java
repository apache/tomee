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

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

@Provider
public class InboundFilter implements ContainerRequestFilter, ContainerResponseFilter {
	
	/**
	 * Incoming (request) filter
	 */
	@Override
	public void filter(ContainerRequestContext ctx) {
		final String PARAM_ID = "app_session_id";
		MultivaluedMap<String, String> queryParams = ctx.getUriInfo().getQueryParameters();
		
		String id = queryParams.getFirst(PARAM_ID);
		if (id == null) {
			Client client = ClientBuilder.newClient();
			WebTarget target = client.target("http://localhost:8080/jaxrs-filter/unauthorized/");
			Builder builder = target.request(MediaType.TEXT_HTML);
			Response response = builder.get();
			ctx.abortWith(response);
		}
	}

	/**
	 * Outbound (response) filter
	 */
	@Override
	public void filter(ContainerRequestContext requestCtx,
			ContainerResponseContext responseCtx) {
		//
	}

}
