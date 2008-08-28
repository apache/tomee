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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.server.ejbd;

import org.apache.openejb.server.ServerService;
import org.apache.openejb.server.ServiceException;
import org.apache.openejb.server.ServicePool;
import org.apache.openejb.loader.SystemInstance;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.net.Socket;
import java.util.Properties;
import java.util.Map;
import java.util.TimerTask;
import java.util.Timer;
import java.util.Date;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.BlockingQueue;
import java.text.SimpleDateFormat;

/**
 * @version $Rev$ $Date$
 */
public class KeepAliveServer implements ServerService {
    private final ServerService service;
    private final long timeout = (1000 * 3);
    private final KeepAliveTimer keepAliveTimer = new KeepAliveTimer(timeout);

    public KeepAliveServer() {
        this(new EjbServer());
    }

    public KeepAliveServer(ServerService service) {
        this.service = service;


        Timer timer = new Timer("KeepAliveTimer", true);
        timer.scheduleAtFixedRate(keepAliveTimer, timeout, timeout / 2);

    }



    public static class KeepAliveTimer extends TimerTask {

        private final Map<Thread, Status> statusMap = new ConcurrentHashMap<Thread, Status>();

        private final long timeout;
        private BlockingQueue<Runnable> queue;

        public KeepAliveTimer(long timeout) {
            this.timeout = timeout;
        }

        public void run() {
            BlockingQueue<Runnable> queue = getQueue();
            if (queue == null) return;

            int backlog = queue.size();
            if (backlog <= 0) return;

            long now = System.currentTimeMillis();

            Collection<Status> statuses = statusMap.values();
            for (Status status : statuses) {

//                System.out.println(""+status);

                if (status.isReading() && now - status.getTime() > timeout){
//                    System.out.println("Thread Interrupt");
                    try {
                        backlog--;
                        status.in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if (backlog <= 0) return;
            }
//            System.out.println("exit");
        }

        private BlockingQueue<Runnable> getQueue() {
            if (queue == null){
                // this can be null if timer fires before service is fully initialized
                ServicePool incoming = SystemInstance.get().getComponent(ServicePool.class);
                if (incoming == null) return null;
                ThreadPoolExecutor threadPool = incoming.getThreadPool();
                queue = threadPool.getQueue();
            }
            return queue;
        }

        public Status setStatus(Status status) {
//            System.out.println("status = " + status);
            return statusMap.put(status.getThread(), status);
        }
    }

    public static class Status {
        private final long time;
        private final boolean reading;
        private final Thread thread;
        private static final SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss.SSS");
        private final InputStream in;

        public boolean isReading() {
            return reading;
        }

        public Thread getThread() {
            return thread;
        }

        public long getTime() {
            return time;
        }

        public Status(boolean reading, InputStream in) {
            this.reading = reading;
            this.thread = Thread.currentThread();
            this.time = System.currentTimeMillis();
            this.in = in;
        }

        public String toString() {
            String msg = "";
            if (reading)
            msg += "READING";
            else msg += "WORKING";
            msg += " "+thread.getName();

            msg += " since "+ format.format(new Date(time));
            return msg;
        }
    }


    public void service(Socket socket) throws ServiceException, IOException {
        InputStream in = new BufferedInputStream(socket.getInputStream());
        OutputStream out = new BufferedOutputStream(socket.getOutputStream());

        try {
            while (true) {
                keepAliveTimer.setStatus(new Status(true, in));
                int i = in.read();
                char c = (char) i;
                if (i == 30){
                    keepAliveTimer.setStatus(new Status(false, null));
                    service.service(new Input(in), new Output(out));
                    out.flush();
                } else {
                    keepAliveTimer.setStatus(new Status(false, null));
                    break;
                }
            }
        } catch (InterruptedIOException e) {
            Thread.interrupted();
        } catch (IOException e) {
        } finally{
            keepAliveTimer.setStatus(new Status(false, null));
//            System.out.println("close socket");
            socket.close();
        }
    }

    public void service(InputStream in, OutputStream out) throws ServiceException, IOException {
    }

    public String getIP() {
        return service.getIP();
    }

    public String getName() {
        return service.getName();
    }

    public int getPort() {
        return service.getPort();
    }

    public void start() throws ServiceException {
        service.start();
    }

    public void stop() throws ServiceException {
        service.stop();
    }

    public void init(Properties props) throws Exception {
        service.init(props);
    }

    public class Input extends java.io.FilterInputStream {

        public Input(InputStream in) {
            super(in);
        }

        public void close() throws IOException {
        }
    }

    public class Output extends java.io.FilterOutputStream {
        public Output(OutputStream out) {
            super(out);
        }

        public void close() throws IOException {
            flush();
        }
    }

}
