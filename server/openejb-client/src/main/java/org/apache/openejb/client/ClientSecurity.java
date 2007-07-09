/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.client;

import javax.security.auth.login.FailedLoginException;
import java.net.URI;
import java.net.URISyntaxException;
import java.rmi.RemoteException;

public class ClientSecurity {
    public static final String IDENTITY_RESOLVER_STRATEGY = "openejb.client.identityResolver";

    private static ServerMetaData server;
    private static IdentityResolver identityResolver;
    private static Object staticClientIdentity;
    private static InheritableThreadLocal<Object> threadClientIdentity = new InheritableThreadLocal<Object>();

    static {
        // determine the server uri
        String serverUri = System.getProperty("openejb.server.uri");

        if (serverUri != null) {
            // determine the server location
            try {
                URI location = new URI(serverUri);
                server = new ServerMetaData(location);
            } catch (Exception e) {
                if (serverUri.indexOf("://") == -1) {
                    try {
                        URI location =  new URI("oejb://" + serverUri);
                        server = new ServerMetaData(location);
                    } catch (URISyntaxException ignored) {
                    }
                }
            }
        }
    }


    public static ServerMetaData getServer() {
        return server;
    }

    public static void setServer(ServerMetaData server) {
        ClientSecurity.server = server;
    }

    /**
     * Login the spedified user using the specified password.  This is a global login for the
     * entire Java Virtural Machine.  If you would like to have a thread scoped login, use
     * ClientSecurity.login(username, password, true);
     * </p>
     * This is the equivalent of ClientSecurity.login(username, password, false);
     * @param username the user to login
     * @param password the password for the user
     * @throws FailedLoginException if the username and password combination are not valid or
     *      if there is a problem communiating with the server
     */
    public static void login(String username, String password) throws FailedLoginException {
        login(username, password, false);
    }

    /**
     * Login the spedified user using the specified password either globally for the
     * entire Java Virtural Machine or scoped to the thread.
     * </p>
     * When using thread scoped login, you should logout in a finally block.  This particularly
     * when using thread pools.  If a thread is returned to the pool with a login attached to the
     * thread the next user of that thread will inherit the thread scoped login.
     *
     * @param username the user to login
     * @param password the password for the user
     * @param threadScoped if true the login is scoped to the thread; otherwise the login is global
     * for the entire Java Virtural Machine
     * @throws FailedLoginException if the username and password combination are not valid or
     *      if there is a problem communiating with the server
     */
    public static void login(String username, String password, boolean threadScoped) throws FailedLoginException {
        Object clientIdentity = directAuthentication(username, password, server);
        if (threadScoped) {
            threadClientIdentity.set(clientIdentity);
        } else {
            staticClientIdentity = clientIdentity;
        }
        identityResolver = new SimpleIdentityResolver();
    }

    /**
     * Clears the thread and global login data.
     */
    public static void logout() {
        threadClientIdentity.set(null);
        staticClientIdentity = null;
    }

    /**
     * This is a helper method for login modules. Directly authenticates with the server using the specified
     * username and password returning the identity token for the client.  This methods does not store the
     * identity token and the caller must arrange for the to be available to the OpenEJB proxies via an
     * IdentityResolver.
     *
     * @param username the username for authentication
     * @param password the password for authentication
     * @param server
     * @return the client identity token
     * @throws FailedLoginException if the username password combination is not valid
     */
    public static Object directAuthentication(String username, String password, ServerMetaData server) throws FailedLoginException {
        return directAuthentication("PropertiesLogin", username, password, server);
    }

    public static Object directAuthentication(String securityRealm, String username, String password, ServerMetaData server) throws FailedLoginException {
        // authenticate
        AuthenticationRequest authReq = new AuthenticationRequest(new RealmPrincipalInfo(securityRealm, username), password);
        AuthenticationResponse authRes;
        try {
            authRes = (AuthenticationResponse) Client.request(authReq, new AuthenticationResponse(), server);
        } catch (RemoteException e) {
            throw (FailedLoginException) new FailedLoginException("Unable to authenticate with server " + server).initCause(e);
        }

        // check the response
        if (authRes.getResponseCode() != ResponseCodes.AUTH_GRANTED) {
            throw new FailedLoginException("This principal is not authenticated.");
        }

        // return the response object
        Object clientIdentity = authRes.getIdentity().getClientIdentity();
        return clientIdentity;
    }

    public static Object getIdentity() {
        return getIdentityResolver().getIdentity();
    }

    public static IdentityResolver getIdentityResolver() {
        if (identityResolver == null) {
            String strategy = System.getProperty(IDENTITY_RESOLVER_STRATEGY);
            if (strategy == null) {
                identityResolver = new JaasIdentityResolver();
            } else {
                // find the strategy class
                ResourceFinder finder = new ResourceFinder("META-INF/");
                Class identityResolverClass;
                try {
                    identityResolverClass = finder.findClass(IdentityResolver.class.getName() + "/" + strategy);
                } catch (Exception e) {
                    throw new IllegalArgumentException("Could not find client identity strategy '" + strategy + "'");
                }

                // verify the interface
                if (!IdentityResolver.class.isAssignableFrom(identityResolverClass)) {
                    throw new IllegalArgumentException("Client identity strategy '" + strategy + "' " +
                            "class '" + identityResolverClass.getName() + "' does not implement the " +
                            "interface '"  + IdentityResolver.class.getSimpleName() + "'");
                }

                // create the class
                try {
                    identityResolver = (IdentityResolver) identityResolverClass.newInstance();
                } catch (Exception e) {
                    throw new IllegalArgumentException("Unable to create client identity strategy '" + strategy + "' " +
                            "class '" + identityResolverClass.getName() + "'", e);
                }
            }

        }
        return identityResolver;
    }


    public static void setIdentityResolver(IdentityResolver identityResolver) {
        ClientSecurity.identityResolver = identityResolver;
    }

    private ClientSecurity() {
    }

    public static class SimpleIdentityResolver implements IdentityResolver {
        public Object getIdentity() {
            Object clientIdentity = threadClientIdentity.get();
            if (clientIdentity == null) {
                clientIdentity = staticClientIdentity;
            }
            return clientIdentity;
        }
    }
}
