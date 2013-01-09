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
package org.apache.openejb.core.security.jaas;

import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.URLs;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.security.Principal;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

public class ScriptLoginModule implements LoginModule {
    private static Logger log = Logger.getInstance(LogCategory.OPENEJB_SECURITY, "org.apache.openejb.util.resources");

    private Subject subject;
    private CallbackHandler callbackHandler;

    private Map<String, ?> options;

    public Set<Principal> principals = new LinkedHashSet<Principal>();

    private UserData userData;

    private class UserData {
        public final String user;
        public final String pass;
        public final Set<String> groups = new HashSet<String>();

        private UserData(String user, String pass) {
            this.user = user;
            this.pass = pass;
        }
    }

    @Override
    public void initialize(Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState, Map<String, ?> options) {
        this.options = options;
        this.subject = subject;
        this.callbackHandler = callbackHandler;
    }

    private UserData getUserData() throws LoginException {
        final Callback[] callbacks = new Callback[2];

        callbacks[0] = new NameCallback("Username: ");
        callbacks[1] = new PasswordCallback("Password: ", false);
        try {
            this.callbackHandler.handle(callbacks);
        } catch (IOException ioe) {
            throw new LoginException(ioe.getMessage());
        } catch (UnsupportedCallbackException uce) {
            throw new LoginException(uce.getMessage() + " not available to obtain information from user");
        }

        final String user = ((NameCallback) callbacks[0]).getName();

        char[] tmpPassword = ((PasswordCallback) callbacks[1]).getPassword();
        if (tmpPassword == null) {
            tmpPassword = new char[0];
        }

        final String password = new String(tmpPassword);

        return new UserData(user, password);
    }

    private File getScriptFile(String path) {
        if (path == null  || "".equals(path)) {
            final File result = new File(System.getProperty("openejb.home"), "conf/loginscript.js");
            if (result.exists()) {
                return result;
            } else {
                return null;
            }
        }

        try {
            final URI uri = URLs.uri(path);
            final File result = new File(uri);
            if (result.exists()) {
                return result;
            }
        } catch (Exception e) {
            // no-op
        }

        {
            final File result = new File(path);
            if (result.exists()) {
                return result;
            }
        }

        {
            final File openEjbConf = new File(System.getProperty("openejb.home"), "conf");
            final File result = new File(openEjbConf, path);
            if (result.exists()) {
                return result;
            }
        }

        return null;
    }

    @Override
    public boolean login() throws LoginException {
        File script = getScriptFile((String) this.options.get("scriptURI"));
        if (script == null) {
            script = getScriptFile(System.getProperty("openejb.ScriptLoginModule.scriptURI"));
            if (script == null) {
                script = getScriptFile(System.getProperty(null));
            }
        }
        if (script == null) {
            throw new LoginException("No login script defined");
        }
        final String scriptText;
        try {
            scriptText = new Scanner(script).useDelimiter("\\Z").next();
        } catch (FileNotFoundException e) {
            throw new LoginException("Invalid login script URI.");
        }

        this.userData = getUserData();

        final ScriptEngineManager manager = new ScriptEngineManager();
        final ScriptEngine engine = manager.getEngineByName((String) this.options.get("engineName"));

        //new context for the execution of this script
        final ScriptContext newContext = new SimpleScriptContext();

        //creating the bidings object for the current execution
        final Bindings bindings = newContext.getBindings(ScriptContext.ENGINE_SCOPE);

        bindings.put("user", this.userData.user);
        bindings.put("password", this.userData.pass);

        final List<String> myGroups;
        try {
            myGroups = (List) engine.eval(scriptText, newContext);
        } catch (ScriptException e) {
            throw new LoginException("Cannot execute login script. Msg: " + e.getMessage());
        }
        this.userData.groups.addAll(myGroups);

        return true;
    }

    @Override
    public boolean commit() throws LoginException {
        this.principals.add(new UserPrincipal(this.userData.user));

        for (String myGroup : this.userData.groups) {
            principals.add(new GroupPrincipal(myGroup));
        }

        this.subject.getPrincipals().addAll(this.principals);

        clear();

        log.debug("commit");
        return true;
    }

    @Override
    public boolean abort() throws LoginException {
        clear();
        log.debug("abort");
        return true;
    }

    @Override
    public boolean logout() throws LoginException {
        this.subject.getPrincipals().removeAll(this.principals);
        this.principals.clear();

        log.debug("logout");
        return true;
    }

    private void clear() {
        this.userData = null;
    }
}
