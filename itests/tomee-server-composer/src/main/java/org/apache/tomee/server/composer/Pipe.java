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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.tomee.server.composer;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

public final class Pipe implements Runnable {

    private final InputStream in;
    private final ByteArrayOutputStream out = new ByteArrayOutputStream();
    private final OutputStream[] cc;

    public Pipe(final InputStream in, final OutputStream... copy) {
        this.in = in;
        this.cc = copy;
    }

    public static Future<Pipe> pipe(final InputStream in, final OutputStream... copy) {
        final Pipe target = new Pipe(in, copy);

        final FutureTask<Pipe> task = new FutureTask<>(target, target);
        final Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();

        return task;
    }

    public synchronized String asString() {
        return new String(out.toByteArray());
    }

    public synchronized void run() {
        try {
            int i = -1;

            while ((i = in.read()) != -1) {
                out.write(i);
                for (final OutputStream stream : cc) {
                    stream.write(i);
                }
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }
}
