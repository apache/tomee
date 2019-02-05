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
package javax.security.enterprise.authentication.mechanism.http;

import javax.security.enterprise.AuthenticationException;
import javax.security.enterprise.AuthenticationStatus;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class HttpAuthenticationMechanismWrapper implements HttpAuthenticationMechanism {
    private final HttpAuthenticationMechanism httpAuthenticationMechanism;

    public HttpAuthenticationMechanismWrapper(final HttpAuthenticationMechanism httpAuthenticationMechanism) {
        this.httpAuthenticationMechanism = httpAuthenticationMechanism;
    }

    public HttpAuthenticationMechanism getWrapped() {
        return httpAuthenticationMechanism;
    }

    @Override
    public AuthenticationStatus validateRequest(HttpServletRequest request,
                                                HttpServletResponse response,
                                                HttpMessageContext httpMessageContext)
            throws AuthenticationException {
        return getWrapped().validateRequest(request, response, httpMessageContext);
    }

    @Override
    public AuthenticationStatus secureResponse(HttpServletRequest request,
                                               HttpServletResponse response,
                                               HttpMessageContext httpMessageContext)
            throws AuthenticationException {
        return getWrapped().secureResponse(request, response, httpMessageContext);
    }

    @Override
    public void cleanSubject(HttpServletRequest request,
                             HttpServletResponse response,
                             HttpMessageContext httpMessageContext) {
        getWrapped().cleanSubject(request, response, httpMessageContext);
    }
}
