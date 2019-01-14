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
package org.apache.tomee.security.client;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import java.io.IOException;
import java.util.Base64;

import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;

public class BasicAuthFilter implements ClientRequestFilter {
    private final String username;
    private final String password;

    public BasicAuthFilter(final String username, final String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public void filter(final ClientRequestContext requestContext) throws IOException {
        requestContext.getHeaders()
                      .add(AUTHORIZATION,
                           "Basic " + new String(Base64.getEncoder().encode((username + ":" + password).getBytes())));
    }
}
