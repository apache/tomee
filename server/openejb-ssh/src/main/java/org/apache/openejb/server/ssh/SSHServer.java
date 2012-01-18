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
package org.apache.openejb.server.ssh;

import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.server.SelfManaging;
import org.apache.openejb.server.ServerService;
import org.apache.openejb.server.ServiceException;
import org.apache.sshd.SshServer;
import org.apache.sshd.common.util.SecurityUtils;
import org.apache.sshd.server.PublickeyAuthenticator;
import org.apache.sshd.server.command.ScpCommandFactory;
import org.apache.sshd.server.jaas.JaasPasswordAuthenticator;
import org.apache.sshd.server.keyprovider.PEMGeneratorHostKeyProvider;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.session.ServerSession;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.security.PublicKey;
import java.util.Properties;

public class SSHServer implements ServerService, SelfManaging {
    private static final String KEY_NAME = System.getProperty("openejb.server.ssh.key", "ssh-key");

    private int port;
    private String bind;
    private String domain;
    private SshServer sshServer;

    @Override
    public void start() throws ServiceException {
        final JaasPasswordAuthenticator authenticator = new JaasPasswordAuthenticator();
        authenticator.setDomain(domain);

        sshServer = SshServer.setUpDefaultServer();
        sshServer.setPort(port);
        sshServer.setHost(bind);
        sshServer.setPasswordAuthenticator(authenticator);

        final String basePath = SystemInstance.get().getBase().getDirectory().getAbsolutePath();
        if (SecurityUtils.isBouncyCastleRegistered()) {
            sshServer.setKeyPairProvider(new PEMGeneratorHostKeyProvider(new File(basePath, KEY_NAME + ".pem").getPath()));
        } else {
            sshServer.setKeyPairProvider(new SimpleGeneratorHostKeyProvider(new File(basePath, KEY_NAME + ".ser").getPath()));
        }

        sshServer.setShellFactory(new GroovyShellFactory());

        try {
            sshServer.start();
        } catch (IOException e) {
            // no-op
        }
    }

    @Override
    public void stop() throws ServiceException {
        try {
            sshServer.stop();
        } catch (Exception e) {
            // no-op
        }
    }

    @Override
    public void service(InputStream in, OutputStream out) throws ServiceException, IOException {

    }

    @Override
    public void service(Socket socket) throws ServiceException, IOException {

    }

    @Override
    public String getName() {
        return "ssh";
    }

    @Override
    public String getIP() {
        return bind;
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public void init(Properties props) throws Exception {
        bind = props.getProperty("bind");
        domain = props.getProperty("domain");
        port = Integer.parseInt(props.getProperty("port"));
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public void setBind(String bind) {
        this.bind = bind;
    }
}
