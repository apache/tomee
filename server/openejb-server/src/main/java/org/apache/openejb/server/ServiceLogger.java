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
package org.apache.openejb.server;

import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.Messages;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Properties;

/**
 * @version $Rev$ $Date$
 */
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

        logger = Logger.getInstance(LogCategory.OPENEJB_SERVER.createChild("service."+getName()), "org.apache.openejb.server.util.resources");

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

    private static Method MDBput = null;
    static {
        try {
            final Class<?> MDC = ServiceLogger.class.getClassLoader().loadClass("org.apache.log4j.MDC");
            MDBput = MDC.getMethod("put", String.class, String.class);
        } catch (Exception e) {
            Logger.getInstance(LogCategory.OPENEJB, ServiceLogger.class.getName())
                    .info("can't find log4j MDC class");
        }
    }
    public static void MDCput(final String key, final String value) {
        if (MDBput != null) {
            try {
                MDBput.invoke(null, key, value);
            } catch (Exception e) {
                // ignored
            }
        }
    }
    
    public void service(Socket socket) throws ServiceException, IOException {
        InetAddress client = socket.getInetAddress();

        MDCput("HOST", client.getHostAddress());
        MDCput("SERVER", getName());

        try {

//            logger.info("[request] "+socket.getPort()+" - "+client.getHostName());
            next.service(socket);
//            logSuccess();
        } catch (Exception e) {
            logger.error("[failure] " + socket.getPort() + " - " + client.getHostAddress() + ": " + e.getMessage());

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
