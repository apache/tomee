/*
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
package org.apache.openejb.daemon;

import org.apache.openejb.cli.Bootstrap;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.server.Server;
import org.apache.openejb.server.ServerRuntimeException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

public class NTService {

    private static final NTService instance = new NTService();
    private final AtomicBoolean running = new AtomicBoolean(false);

    /**
     * Called by Apache Daemon
     *
     * @param args Start arguments
     */
    public static void start(final String[] args) {
        try {
            instance.startImpl(args);
        } catch (Throwable e) {
            e.printStackTrace(System.err);
        }
    }

    /**
     * Called by Apache Daemon
     *
     * @param args Stop arguments - Not used
     */
    public static void stop(final String[] args) {
        try {
            instance.stopImpl();
        } catch (Throwable e) {
            e.printStackTrace(System.err);
        }
    }

    private NTService() {
    }

    private void startImpl(String[] args) {
        if (!running.getAndSet(true)) {

            if (null == args) {
                args = new String[0];
            }

            if (null == System.getProperty("openejb.home")) {
                System.setProperty("openejb.home", System.getProperty("user.dir"));
            }

            final ArrayList<String> list = new ArrayList<String>();
            list.addAll(Arrays.asList(args));

            if (!list.contains("start")) {
                list.add("start");
            }

            try {
                System.out.println("Starting NTService: " + list);
                Bootstrap.main(list.toArray(new String[list.size()]));

                //The process has finished
                running.set(false);

            } catch (Exception e) {
                running.set(false);
                throw new ServerRuntimeException("Failed to Bootstrap OpenEJB", e);
            }

        }
    }

    private void stopImpl() {
        if (running.getAndSet(false)) {

            final Server server = SystemInstance.get().getComponent(Server.class);

            if (null != server) {
                try {
                    System.out.println("Stopping NTService");
                    server.stop();
                } catch (Exception e) {

                    //Failed to stop
                    running.set(true);
                    e.printStackTrace(System.err);
                }
            }
        }
    }

}
