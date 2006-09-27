package org.apache.openejb.server.admin;

import java.io.*;
import java.net.*;
import java.util.*;

import org.apache.openejb.server.ServerService;
import org.apache.openejb.server.ServiceManager;
import org.apache.openejb.server.ServiceException;
import org.apache.openejb.client.RequestMethods;

public class AdminDaemon implements ServerService {

    public void init(Properties props) throws Exception {
    }

    public void service(Socket socket) throws ServiceException, IOException {
        InputStream in = null;
        InetAddress clientIP = null;

        try {
            in = socket.getInputStream();
            clientIP = socket.getInetAddress();

            byte requestType = (byte) in.read();

            if (requestType == -1) {
                return;
            }

            switch (requestType) {
                case RequestMethods.STOP_REQUEST_Quit:
                case RequestMethods.STOP_REQUEST_quit:
                case RequestMethods.STOP_REQUEST_Stop:
                case RequestMethods.STOP_REQUEST_stop:
                    ServiceManager.getManager().stop();

            }

        } catch (SecurityException e) {

        } catch (Throwable e) {

        } finally {
            try {
                if (in != null) in.close();
                if (socket != null) socket.close();
            } catch (Throwable t) {

            }
        }
    }

    public void service(InputStream in, OutputStream out) throws ServiceException, IOException {
        throw new UnsupportedOperationException("Method not implemented: service(InputStream in, OutputStream out)");
    }
    
    public void start() throws ServiceException {
    }

    public void stop() throws ServiceException {
    }

    public int getPort() {
        return 0;
    }

    public String getIP() {
        return "";
    }

    public String getName() {
        return "admin thread";
    }

}
