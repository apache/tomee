package org.apache.openejb.server;

import java.io.*;
import java.net.*;
import java.util.*;

import org.apache.openejb.util.*;

public class ServiceLogger implements ServerService {

    Messages messages = new Messages("org.apache.openejb.server.util.resources");
    Logger logger;

    boolean logOnSuccess;
    boolean logOnFailure;

    ServerService next;

    public ServiceLogger(ServerService next) {
        this.next = next;
    }

    public void init(Properties props) throws Exception {

        String logCategory = "OpenEJB.server.service." + getName();

        logger = Logger.getInstance(logCategory, "org.apache.openejb.server.util.resources");

        next.init(props);
    }

    public void start() throws ServiceException {

        next.start();
    }

    public void stop() throws ServiceException {

        next.stop();
    }

    public void service(InputStream in, OutputStream out) throws ServiceException, IOException {
        throw new UnsupportedOperationException("service(in,out)");
    }

    
    public void service(Socket socket) throws ServiceException, IOException {

        InetAddress client = socket.getInetAddress();
        org.apache.log4j.MDC.put("HOST", client.getHostName());
        org.apache.log4j.MDC.put("SERVER", getName());

        try {

//            logger.info("[request] "+socket.getPort()+" - "+client.getHostName());
            next.service(socket);
//            logSuccess();
        } catch (Exception e) {
            logger.error("[failure] " + socket.getPort() + " - " + client.getHostName() + ": " + e.getMessage());

            e.printStackTrace();
        }
    }

    private void logIncoming() {
        logger.info("incomming request");
    }

    private void logSuccess() {
        logger.info("successful request");
    }

    private void logFailure(Exception e) {
        logger.error(e.getMessage());
    }

    public String getName() {
        return next.getName();
    }

    public String getIP() {
        return next.getIP();
    }

    public int getPort() {
        return next.getPort();
    }

}
