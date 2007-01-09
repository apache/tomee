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
package org.apache.openejb.server.telnet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Properties;

import org.apache.openejb.server.ServiceException;

public class TelnetServer implements org.apache.openejb.server.ServerService {


    public void init(Properties props) throws Exception {
    }

    public void service(Socket socket) throws ServiceException, IOException {
        InputStream telnetIn = null;
        PrintStream telnetOut = null;

        try {
            InputStream in = socket.getInputStream();
            OutputStream out = socket.getOutputStream();

            telnetIn = new TelnetInputStream(in, out);
            telnetOut = new TelnetPrintStream(out);

            telnetOut.println("OpenEJB Remote Server Console");
            telnetOut.println("type \'help\' for a list of commands");


            TextConsole shell = new TextConsole();
            shell.exec(telnetIn, telnetOut);

        } catch (Throwable t) {

        } finally {
            if (telnetIn != null)
                telnetIn.close();
            if (telnetOut != null)
                telnetOut.close();
            if (socket != null) socket.close();

        }
    }

    public void service(InputStream in, OutputStream out) throws ServiceException, IOException {
        throw new UnsupportedOperationException("Method not implemented: service(InputStream in, OutputStream out)");
    }
    
    public void start() throws ServiceException {
    }

    public void stop() throws ServiceException {
    }

    public String getName() {
        return "telnet";
    }

    public int getPort() {
        return 0;
    }

    public String getIP() {
        return "";
    }

}
