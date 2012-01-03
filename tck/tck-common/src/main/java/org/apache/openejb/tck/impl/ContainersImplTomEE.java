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
package org.apache.openejb.tck.impl;

import org.apache.openejb.assembler.Deployer;
import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.client.RemoteInitialContextFactory;
import org.apache.openejb.config.RemoteServer;
import org.apache.openejb.config.ValidationException;
import org.apache.openejb.loader.Options;
import org.jboss.testharness.api.DeploymentException;
import org.jboss.testharness.spi.Containers;

import javax.naming.Context;
import javax.naming.InitialContext;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @version $Rev$ $Date$
 */
public class ContainersImplTomEE extends AbstractContainers implements Containers {
    private static int count = 0;
    private final RemoteServer server;
    private Deployer deployer = null;
    private Exception exception;
    private AppInfo appInfo;
    private File currentFile = null;

    private Deployer lookup() {
        final Options options = new Options(System.getProperties());
        Properties props = new Properties();
        props.put(Context.INITIAL_CONTEXT_FACTORY, RemoteInitialContextFactory.class.getName());
        String port = System.getProperty("server.http.port");
        if (port != null) {
            props.put(Context.PROVIDER_URL, options.get(Context.PROVIDER_URL,"http://localhost:" + port + "/openejb/ejb"));
        } else {
            throw new RuntimeException("Please set the tomee port as a system property");
        }

        final String deployerJndi = System.getProperty("openejb.deployer.jndiname", "openejb/DeployerBusinessRemote");

        try {
            InitialContext context = new InitialContext(props);
            return (Deployer) context.lookup(deployerJndi);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public ContainersImplTomEE() {
        System.out.println("ContainersImpl=" + ContainersImplTomEE.class.getName());
        System.out.println("Initialized ContainersImplTomEE " + (++count));
        server = new RemoteServer();
    }
    @Override
    public boolean deploy(InputStream archive, String name) throws IOException {
        exception = null;
        appInfo = null;

        System.out.println("Deploying " + archive + " with name " + name);

        currentFile = getFile(name);
        System.out.println(currentFile);
        writeToFile(currentFile, archive);
        try {
            if (deployer == null) {
                deployer = lookup();
            }
            appInfo = deployer.deploy(currentFile.getAbsolutePath());
        } catch (Exception ex) {
            Exception e = ex;
            if (e.getCause() instanceof ValidationException) {
                e = (Exception) e.getCause();
            }

            if (name.contains(".broken.")) {
                // Tests that contain the name '.broken.' are expected to fail deployment
                // This is how the TCK verifies the container is doing the required error checking
                exception = (DeploymentException) new DeploymentException("deploy failed").initCause(e);
            } else {
                // This on the other hand is not good ....
                System.out.println("FIX Deployment of " + name);
                e.printStackTrace();
                exception = e;
            }

            return false;
        }
        return true;
    }

    @Override
    public DeploymentException getDeploymentException() {
        try {
            return (DeploymentException) exception;
        } catch (Exception e) {
            System.out.println("BADCAST");
            return new DeploymentException("", exception);
        }
    }

    @Override
    public void undeploy(String name) throws IOException {
        if (appInfo == null) {
            if (!(exception instanceof DeploymentException)) {
                System.out.println("Nothing to undeploy" + name);
            }
            return;
        }

        System.out.println("Undeploying " + name);
        try {
            deployer.undeploy(appInfo.path);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        File toDelete;
        if (currentFile != null && (toDelete = currentFile.getParentFile()).exists()) {
            System.out.println("deleting " + toDelete.getAbsolutePath());
            delete(toDelete);
        }
    }

    protected File getFile(String name) {
        final File dir = new File(tmpDir, Math.random()+"");
        dir.mkdir();
        dir.deleteOnExit();
        return new File(dir, name);
    }

    @Override
    public void setup() throws IOException {
        System.out.println("Setup called");
        server.start();
    }
    @Override
    public void cleanup() throws IOException {
        System.out.println("Cleanup called");
        server.stop();
    }
}
