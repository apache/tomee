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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.server.ssh;

import org.apache.sshd.common.Factory;
import org.apache.sshd.server.Command;

import javax.security.auth.login.LoginContext;

public class OpenEJBShellFactory implements Factory<Command> {
    private String bind;
    private int port;
    private ThreadLocal<String> username = new ThreadLocal<String>();
    private ThreadLocal<LoginContext> loginContext = new ThreadLocal<LoginContext>();

    public OpenEJBShellFactory(String bind, int port) {
        this.bind = bind;
        this.port = port;
    }

    @Override
    public Command create() {
        return new OpenEJBCommands(bind, port, username.get(), loginContext.get());
    }

    public void setUsername(final String username) {
        this.username.set(username);
    }

    public void setLoginContext(final LoginContext lc) {
        loginContext.set(lc);
    }
}
