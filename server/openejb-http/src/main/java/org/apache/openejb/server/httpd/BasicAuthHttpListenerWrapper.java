/**
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

package org.apache.openejb.server.httpd;

import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.SecurityService;
import org.apache.openejb.util.Base64;

import javax.security.auth.login.LoginException;

public class BasicAuthHttpListenerWrapper implements HttpListener {
    private HttpListener httpListener;
    private String realmName;

    public BasicAuthHttpListenerWrapper(HttpListener httpListener, String realmName) {
        this.httpListener = httpListener;
        this.realmName = realmName;
    }

    public void onMessage(HttpRequest request, HttpResponse response) throws Exception {
        Object token = null;

        String auth = request.getHeader("Authorization");
        if (auth != null && auth.length() > 0) {
            if (auth.toUpperCase().startsWith("BASIC ")) {
                auth = auth.substring(6);
                String decoded = new String(Base64.decodeBase64(auth.getBytes()));
                String[] parts = decoded.split(":");
                if (parts != null && parts.length == 2) {
                    String username = parts[0];
                    String password = parts[1];

                    try {
                        final SecurityService securityService = getSecurityService();
                        token = securityService.login(realmName, username, password);
                        if (token != null) {
                            securityService.associate(token);
                        }
                    } catch (LoginException e) {
                        // login failed, return 401
                    }
                }
            }
        }

        if (token != null || HttpRequest.Method.GET.name().equals(request.getMethod())) {
            httpListener.onMessage(request, response);
        } else {
            // login failed,  return 401
        }

        if (token != null) {
           getSecurityService().disassociate();
        }
    }

    private SecurityService getSecurityService() {
        final SecurityService securityService = SystemInstance.get().getComponent(SecurityService.class);
        return securityService;
    }

}
