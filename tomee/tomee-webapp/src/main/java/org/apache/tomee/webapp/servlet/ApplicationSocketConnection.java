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

package org.apache.tomee.webapp.servlet;

import com.google.gson.Gson;
import org.apache.catalina.websocket.StreamInbound;
import org.apache.catalina.websocket.WsOutbound;
import org.apache.openejb.assembler.util.User;
import org.apache.tomee.webapp.command.CommandExecutor;
import org.apache.tomee.webapp.command.CommandSession;
import org.apache.tomee.webapp.command.UserNotAuthenticated;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.CharBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class ApplicationSocketConnection extends StreamInbound implements CommandSession {
    private Gson gson = new Gson();
    private Map<String, Object> attributes = new HashMap<String, Object>();

    private Context context;

    private String readParam(Reader in) throws IOException {

        try {
            final StringBuilder buf = new StringBuilder();
            for (int c = in.read(); c != -1; c = in.read()) {
                buf.append((char) c);
            }

            return buf.toString();
        } finally {
            try {
                in.close();
            } catch (Exception e) {
                // ignored
            }
        }
    }

    @Override
    protected void onBinaryData(InputStream inputStream) throws IOException {
        throw new UnsupportedOperationException("Binary message not supported");
    }

    protected WsOutbound getOutputObject() {
        return getWsOutbound();
    }

    @Override
    protected void onTextData(Reader in) throws IOException {
        final String params = readParam(in);
        final CommandExecutor executor = new CommandExecutor();

        final Map<String, Object> result = executor.execute(this, params);
        getOutputObject().writeTextMessage(CharBuffer.wrap(gson.toJson(result)));
    }

    @Override
    public Context login(String user, String pass) {
        Properties props = new Properties();
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

    @Override
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

    @Override
    public Object get(String key) {
        return this.attributes.get(key);
    }

    @Override
    public void set(String key, Object value) {
        this.attributes.put(key, value);
    }
}
