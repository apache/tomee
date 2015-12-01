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
package org.apache.openejb.util;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class LogStreamAsync implements LogStream, Closeable {

    private static final LinkedBlockingQueue<Message> log = new LinkedBlockingQueue<Message>();
    private static final Thread t = new Thread(new Consumer(LogStreamAsync.log), "LogStreamAsync.Thread");
    private static final AtomicBoolean started = new AtomicBoolean(false);
    private final LogStream ls;

    private enum level {
        fatal,
        error,
        warn,
        info,
        debug,
        quit,
    }

    public LogStreamAsync(final LogStream ls) {
        this.ls = ls;

        if (!started.getAndSet(true)) {
            t.setDaemon(true);
            t.start();
        }
    }

    @Override
    public void close() throws IOException {
        LogStreamAsync.log.clear();
        try {
            LogStreamAsync.log.put(new Message(this.ls, level.quit, ""));
        } catch (final InterruptedException e) {
            //Ignore
        }
    }

    @Override
    public boolean isFatalEnabled() {
        return ls.isFatalEnabled();
    }

    @Override
    public void fatal(final String message) {
        this.log(level.fatal, message);
    }

    @Override
    public void fatal(final String message, final Throwable t) {
        this.log(level.fatal, message, t);
    }

    @Override
    public boolean isErrorEnabled() {
        return ls.isErrorEnabled();
    }

    @Override
    public void error(final String message) {
        this.log(level.error, message);
    }

    @Override
    public void error(final String message, final Throwable t) {
        this.log(level.error, message, t);
    }

    @Override
    public boolean isWarnEnabled() {
        return ls.isWarnEnabled();
    }

    @Override
    public void warn(final String message) {
        this.log(level.warn, message);
    }

    @Override
    public void warn(final String message, final Throwable t) {
        this.log(level.warn, message, t);
    }

    @Override
    public boolean isInfoEnabled() {
        return ls.isInfoEnabled();
    }

    @Override
    public void info(final String message) {
        this.log(level.info, message, null);
    }

    @Override
    public void info(final String message, final Throwable t) {
        this.log(level.info, message, null);
    }

    @Override
    public boolean isDebugEnabled() {
        return ls.isDebugEnabled();
    }

    @Override
    public void debug(final String message) {
        this.log(level.debug, message);
    }

    @Override
    public void debug(final String message, final Throwable t) {
        this.log(level.debug, message, t);
    }

    public void log(final level l, final String s) {
        this.log(l, s, null);
    }

    public void log(final level l, final String s, final Throwable t) {
        try {
            LogStreamAsync.log.put(new Message(this.ls, l, s, t));
        } catch (final InterruptedException e) {
            //Ignore
        }
    }

    private static final class Message {

        private final LogStream ls;
        private final level l;
        private final String s;
        private final Throwable t;

        private Message(final LogStream ls, final level l, final String s) {
            this(ls, l, s, null);
        }

        private Message(final LogStream ls, final level l, final String s, final Throwable t) {
            this.ls = ls;
            this.l = l;
            this.s = s;
            this.t = t;
        }
    }

    private static final class Consumer implements Runnable {

        private final BlockingQueue<Message> queue;

        private Consumer(final BlockingQueue<Message> queue) {
            this.queue = queue;
        }

        public void run() {

            try {
                Message msg;
                while (!level.quit.equals((msg = queue.take()).l)) {
                    final Throwable t = msg.t;

                    if (null != t) {
                        switch (msg.l) {
                            case fatal:
                                msg.ls.fatal(msg.s, t);
                                break;
                            case error:
                                msg.ls.error(msg.s, t);
                                break;
                            case warn:
                                msg.ls.warn(msg.s, t);
                                break;
                            case info:
                                msg.ls.info(msg.s, t);
                                break;
                            case debug:
                                msg.ls.debug(msg.s, t);
                                break;
                        }
                    } else {
                        switch (msg.l) {
                            case fatal:
                                msg.ls.fatal(msg.s);
                                break;
                            case error:
                                msg.ls.error(msg.s);
                                break;
                            case warn:
                                msg.ls.warn(msg.s);
                                break;
                            case info:
                                msg.ls.info(msg.s);
                                break;
                            case debug:
                                msg.ls.debug(msg.s);
                                break;
                        }
                    }
                }
            } catch (final InterruptedException e) {
                //Exit
            }
        }
    }
}