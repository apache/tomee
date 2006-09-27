package org.apache.openejb.server;

import java.io.*;
import java.net.*;
import java.util.*;

public class ServiceDaemon implements ServerService, Runnable {

    ServerService next;

    Properties props;
    String ip;
    int port;

    ServerSocket serverSocket;

    boolean stop = true;

    public ServiceDaemon(ServerService next) {
        this.next = next;
    }

    public ServiceDaemon(ServerService next, int port, String ip) {
        this.port = port;
        this.ip = ip;
        this.next = next;
    }

    public void init(Properties props) throws Exception {

        this.props = props;

        String p = props.getProperty("port");
        ip = props.getProperty("bind");

        port = Integer.parseInt(p);

        next.init(props);
    }

    public void start() throws ServiceException {
        synchronized (this) {

            if (!stop)
                return;

            stop = false;

            try {
//                serverSocket = new ServerSocket(port, 20, InetAddress.getByName(ip));
                serverSocket = new ServerSocket(port, 20);
                port = serverSocket.getLocalPort();
                ip = serverSocket.getInetAddress().getHostAddress();

                Thread d = new Thread(this);
                d.setName("service." + next.getName() + "@" + d.hashCode());
                d.setDaemon(true);
                d.start();
            } catch (Exception e) {
                throw new ServiceException("Service failed to start.", e);

            }

            next.start();
        }
    }

    public void stop() throws ServiceException {

        synchronized (this) {

            if (stop)
                return;

            stop = true;
            try {
                this.notifyAll();
            } catch (Throwable t) {
                t.printStackTrace();

                // Received exception: "+t.getClass().getName()+" :
                // "+t.getMessage());
            }

            next.stop();
        }
    }

    public void service(InputStream in, OutputStream out) throws ServiceException, IOException {
        throw new UnsupportedOperationException("service(in,out)");
    }

    public synchronized void service(final Socket socket)
            throws ServiceException, IOException {
        Thread d = new Thread(new Runnable() {
            public void run() {
                try {
                    next.service(socket);
                } catch (SecurityException e) {

                } catch (Throwable e) {

                } finally {
                    try {
                        if (socket != null)
                            socket.close();
                    } catch (Throwable t) {

                        // connection with client: "+t.getMessage());
                    }
                }
            }
        });
        d.setDaemon(true);
        d.start();
    }

    public String getName() {
        return next.getName();
    }

    public String getIP() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public void run() {

        Socket socket = null;

        while (!stop) {
            try {
                socket = serverSocket.accept();
                socket.setTcpNoDelay(true);
                if (!stop) service(socket);
            } catch (SecurityException e) {

            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }
}
