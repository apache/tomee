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

package org.apache.tomee.webapp;

import org.apache.openejb.assembler.util.User;
import org.apache.tomee.webapp.command.UserNotAuthenticated;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class Application {
    private static final Application INSTANCE = new Application();

    private final Map<String, Session> sessions = new HashMap<String, Session>();

    private Application() {
        //singleton
    }

    public static Application getInstance() {
        return INSTANCE;
    }

    public Session getExistingSession(String id) {
        Session session;
        synchronized (this.sessions) {
            session = this.sessions.get(id);
        }
        return session;
    }

    public Session getSession(String id) {
        Session session;
        synchronized (this.sessions) {
            session = this.sessions.get(id);
            if (session == null) {
                session = new Session();
                this.sessions.put(id, session);
            }
        }
        return session;
    }

    public void removeSession(String id) {
        synchronized (sessions) {
            sessions.remove(id);
        }
    }

    public class Session {
        private Context context;

        public Context getContext() {
            return context;
        }

        public Context login(String user, String pass) {
            final Properties props = new Properties();
            props.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.openejb.client.RemoteInitialContextFactory");
            props.put("java.naming.provider.url", "http://127.0.0.1:8080/tomee/ejb");
            props.setProperty(Context.SECURITY_PRINCIPAL, user);
            props.setProperty(Context.SECURITY_CREDENTIALS, pass);
            try {
                final Context context = new InitialContext(props);
                this.context = context;
            } catch (NamingException e) {
                this.context = null;
            }
            return context;
        }

        public void logout() {
            if (this.context == null) {
                return;
            }
            try {
                this.context.close();
            } catch (Exception e) {
                //NO-OP
            }
            this.context = null;
        }

        public void assertAuthenticated() throws UserNotAuthenticated {
            if (this.context == null) {
                throw new UserNotAuthenticated();
            }

            try {
                final User user = (User) this.context.lookup("openejb/UserBusinessRemote");
                user.adminOnly();
            } catch (Exception e) {
                throw new UserNotAuthenticated(e);
            }
        }

        public String getUserName() {
            if (this.context == null) {
                return null;
            }

            try {
                final User user = (User) this.context.lookup("openejb/UserBusinessRemote");
                return user.getUserName();
            } catch (Exception e) {
                throw new TomeeException(e);
            }
        }
    }
}
