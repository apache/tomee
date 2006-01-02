package org.openejb.server;

import java.io.*;
import java.net.*;
import java.util.*;
import org.openejb.*;

public class ServicePool implements ServerService {

    ServerService next;

    public ServicePool(ServerService next){
        this.next = next;
    }

    public void init(Properties props) throws Exception{

        next.init(props);
    }

    public void start() throws ServiceException{

        next.start();
    }

    public void stop() throws ServiceException{

        next.stop();
    }

    public void service(Socket socket) throws ServiceException, IOException{

        next.service(socket);
    }

    public String getName(){
        return next.getName();
    }

    public String getIP(){
        return next.getIP();
    }

    public int getPort(){
        return next.getPort();
    }

}
