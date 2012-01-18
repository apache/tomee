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

import org.apache.openejb.server.cli.CliRunnable;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class OpenEJBCommands extends CliRunnable implements Command, Runnable {
    private ExitCallback cbk;

    public OpenEJBCommands(String bind, int port, String username) {
        super(bind, port, username, "\r\n"); // don't use os line.separator
    }

    @Override
    public void setInputStream(InputStream in) {
        super.setInputStream(in);
    }

    @Override
    public void setOutputStream(OutputStream out) {
        super.setOutputStream(out);
    }

    @Override
    public void setErrorStream(OutputStream err) {
        super.setErrorStream(err);
    }

    @Override
    public void destroy() {
        super.destroy();
    }

    @Override
    public void setExitCallback(ExitCallback callback) {
        cbk = callback;
    }

    @Override
    public void start(Environment env) throws IOException {
        start();
    }

    @Override
    public void run() {
        try {
            super.run();
        } finally {
            cbk.onExit(0);
        }
    }
}
