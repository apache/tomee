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

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.logging.Logger;

public class ClientLoginModule implements LoginModule {
    private static final Logger log = Logger.getLogger("OpenEJB.client");
    private Subject subject;
    private CallbackHandler callbackHandler;
    private String serverUri;
    private boolean debug;

    private String user;
    private Object clientIdentity;
    private ClientIdentityPrincipal principal;
    private String realmNameSeparator;
    private String realmName;

    public void initialize(Subject subject, CallbackHandler callbackHandler, Map sharedState, Map options) {
        this.subject = subject;
        this.callbackHandler = callbackHandler;

        // determine the server uri
        serverUri = System.getProperty("openejb.server.uri");
        if (serverUri == null) {
            serverUri = (String) options.get("openejb.server.uri");
        }

        this.debug = "true".equalsIgnoreCase((String) options.get("debug"));
        if (debug) {
            log.config("Initialized ClientLoginModule: debug=" + debug);
        }

        if (options.containsKey("RealmNameSeparator")){
            realmNameSeparator = (String)options.get("RealmNameSeparator");
        }

        if (options.containsKey("RealmName")){
            realmName = (String)options.get("RealmName");
        }
    }

    public boolean login() throws LoginException {
        // determine the server location
        URI location = null;
        try {
            location = new URI(serverUri);
        } catch (Exception e) {
            if (serverUri.indexOf("://") == -1) {
                try {
                    location = new URI("foo://" + serverUri);
                } catch (URISyntaxException giveUp) {
                    throw new LoginException("Invalid openejb.server.uri " + serverUri);
                }
            }
        }
        ServerMetaData server = new ServerMetaData(location);

        // create the callbacks
        Callback[] callbacks = new Callback[2];
        callbacks[0] = new NameCallback("Username: ");
        callbacks[1] = new PasswordCallback("Password: ", false);

        // get the call back values (username and password)
        try {
            callbackHandler.handle(callbacks);
        } catch (IOException ioe) {
            throw new LoginException(ioe.getMessage());
        } catch (UnsupportedCallbackException uce) {
            throw new LoginException(uce.getMessage() + " not available to obtain information from user");
        }
        user = ((NameCallback) callbacks[0]).getName();
        char[] tmpPassword = ((PasswordCallback) callbacks[1]).getPassword();
        if (tmpPassword == null) tmpPassword = new char[0];

        if (realmNameSeparator != null){
            String[] strings = user.split(realmNameSeparator);
            if (strings.length == 2){
                realmName = strings[0];
                user = strings[1];
            }
        }

        if (realmName != null) {
            clientIdentity = ClientSecurity.directAuthentication(realmName, user, new String(tmpPassword), server);
        } else {
            clientIdentity = ClientSecurity.directAuthentication(user, new String(tmpPassword), server);
        }

        if (debug) {
            log.config("login " + user);
        }
        return true;
    }

    public boolean commit() throws LoginException {
        principal = new ClientIdentityPrincipal(user, clientIdentity);
        subject.getPrincipals().add(principal);

        if (debug) {
            log.config("commit");
        }
        return true;
    }

    public boolean abort() throws LoginException {
        clear();

        if (debug) {
            log.config("abort");
        }
        return true;
    }

    public boolean logout() throws LoginException {
        subject.getPrincipals().remove(principal);

        if (debug) {
            log.config("logout");
        }
        return true;
    }

    private void clear() {
        user = null;
        clientIdentity = null;
        principal = null;
    }
}
