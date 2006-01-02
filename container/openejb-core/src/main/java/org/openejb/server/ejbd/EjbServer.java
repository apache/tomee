package org.openejb.server.ejbd;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Properties;

import org.openejb.util.Messages;
import org.openejb.util.Logger;
import org.openejb.server.ServiceException;
import org.openejb.server.ServerFederation;
import org.openejb.client.RequestMethods;

public class EjbServer implements org.openejb.server.ServerService {

    EjbDaemon server;

    public void init(Properties props) throws Exception {
        server = EjbDaemon.getEjbDaemon();
        server.init(props);
    }

    public void service(Socket socket) throws ServiceException, IOException {
        ServerFederation.setApplicationServer(server);
        server.service(socket);

    }

    public void start() throws ServiceException {
    }

    public void stop() throws ServiceException {
    }

    public String getName() {
        return "ejbd";
    }

    public int getPort() {
        return 0;
    }

    public String getIP() {
        return "";
    }

}
