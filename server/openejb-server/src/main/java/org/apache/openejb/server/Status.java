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

import org.apache.openejb.loader.SystemInstance;

import java.io.OutputStream;
import java.net.Socket;

@SuppressWarnings("UseOfSystemOutOrSystemErr")
public class Status {

    public static void main(final String[] args) {
        //        System.exit(new Start().start()?0:1);
        new Status().status();
    }

    public boolean status() {
        if (!connect()) {
            System.out.println(":: server is stopped ::");
            return false;
        } else {
            System.out.println(":: server is started ::");
            return true;
        }
    }

    private boolean connect() {
        return connect(1);
    }

    private boolean connect(int tries) {

        Socket socket = null;
        OutputStream out = null;

        try {
            final int port = SystemInstance.get().getOptions().get("ejbd.port", 4201);
            socket = new Socket("localhost", port);
            out = socket.getOutputStream();
        } catch (Exception e) {
            if (tries < 2) {
                return false;
            } else {
                try {
                    Thread.sleep(2000);
                } catch (Exception e2) {
                    e.printStackTrace();
                }
                return connect(--tries);
            }
        } finally {
            if (null != out) {
                try {
                    out.close();
                } catch (Throwable t) {
                    //Ignore
                }
            }

            if (null != socket) {
                try {
                    socket.close();
                } catch (Throwable t) {
                    //Ignore
                }
            }
        }

        return true;
    }
}
