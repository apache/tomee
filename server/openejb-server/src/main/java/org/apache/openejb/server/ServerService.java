package org.apache.openejb.server;

import java.io.*;
import java.net.*;
import java.util.*;

import org.apache.openejb.spi.Service;

public interface ServerService extends Service {

    public void init(Properties props) throws Exception;

    public void start() throws ServiceException;

    public void stop() throws ServiceException;

    public void service(InputStream in, OutputStream out) throws ServiceException, IOException;

    public void service(Socket socket) throws ServiceException, IOException;

    public String getName();

    public String getIP();

    public int getPort();

}
