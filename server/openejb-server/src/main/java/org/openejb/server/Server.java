package org.openejb.server;

import java.util.Properties;

import org.openejb.OpenEJB;
import org.openejb.util.Logger;
import org.openejb.util.Messages;
import org.openejb.util.SafeToolkit;

public class Server implements org.openejb.spi.Service {

    private SafeToolkit toolkit = SafeToolkit.getToolkit("OpenEJB EJB Server");
    private Messages _messages = new Messages("org.openejb.server");
    private Logger logger = Logger.getInstance("OpenEJB.server.remote", "org.openejb.server");

    Properties props;

    static Server server;
    private ServiceManager manager;

    public static Server getServer() {
        if (server == null) {
            server = new Server();
        }

        return server;
    }

    public void init(java.util.Properties props) throws Exception {
        this.props = props;

        OpenEJB.init(props, new ServerFederation());

        if (System.getProperty("openejb.nobanner") == null) {
            System.out.println("[init] OpenEJB Remote Server");
        }

        manager = ServiceManager.getManager();
        manager.init();
    }

    public void start() throws Exception {
        manager.start();
    }

    public void stop() throws Exception {
        manager.stop();
    }
}

