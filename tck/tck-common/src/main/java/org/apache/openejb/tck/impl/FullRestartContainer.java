package org.apache.openejb.tck.impl;

import org.apache.openejb.client.RemoteInitialContextFactory;
import org.apache.openejb.config.RemoteServer;
import org.apache.openejb.loader.Options;
import org.apache.tomee.catalina.facade.ExceptionManagerFacade;
import org.jboss.testharness.api.DeploymentException;
import org.jboss.testharness.spi.Containers;

import javax.naming.Context;
import javax.naming.InitialContext;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * flow:
 * - copy file
 * - start the server
 * - stop the server
 * - remove the file
 */
public class FullRestartContainer extends AbstractContainers implements Containers {
    private static final File WEBAPP_DIR = new File(System.getProperty("openejb.home"), "webapps/");

    private RemoteServer server;
    private Exception exception;
    private File currentFile;

    @Override
    public DeploymentException getDeploymentException() {
        if (exception instanceof DeploymentException) {
            return (DeploymentException) exception;
        }
        System.out.println("BADCAST");
        return new DeploymentException("", exception);
    }

    @Override
    public boolean deploy(InputStream archive, String name) throws IOException {
        currentFile = new File(WEBAPP_DIR, name);
        System.out.println(currentFile);
        writeToFile(currentFile, archive);

        server = new RemoteServer(20, true);
        try {
            server.start();
        } catch (RuntimeException e) {
            e.printStackTrace();
            throw e;
        }
        return (exception = lookup().exception()) == null;
    }

    @Override
    public void undeploy(String name) throws IOException {
        server.destroy();
        delete(currentFile);
    }

    @Override
    public void setup() throws IOException {
        // no-op
    }

    @Override
    public void cleanup() throws IOException {
        // no-op
    }

    private ExceptionManagerFacade lookup() {
        final Options options = new Options(System.getProperties());
        final Properties props = new Properties();
        props.put(Context.INITIAL_CONTEXT_FACTORY, RemoteInitialContextFactory.class.getName());
        String port = System.getProperty("server.http.port");
        if (port != null) {
            System.out.println("provider url = " + "http://localhost:" + port + "/openejb/ejb");
            props.put(Context.PROVIDER_URL, options.get(Context.PROVIDER_URL,"http://localhost:" + port + "/openejb/ejb"));
        } else {
            throw new RuntimeException("Please set the tomee port as a system property");
        }

        try {
            InitialContext context = new InitialContext(props);
            return (ExceptionManagerFacade) context.lookup("openejb/ExceptionManagerFacadeRemote");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
