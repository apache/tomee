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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

/**
 * @version $Rev$ $Date$
 */
public final class Tee implements Runnable {

    private final InputStream in;
    private final OutputStream[] out;

    public Tee(InputStream in, OutputStream... out) {
        this.in = in;
        this.out = out;
    }

    public static InputStream read(Process process) {
//        pipe(process.getErrorStream(), System.err);
        return process.getInputStream();

//        try {
//
//            final PipedOutputStream pipe = new PipedOutputStream();
//            final PipedInputStream snk = new PipedInputStream(pipe, 32*32*32);
//            run(new Tee(process.getInputStream(), System.out, pipe));
//            return snk;
//        } catch (IOException e) {
//            throw new IllegalStateException(e);
//        }
    }

    public static void pipe(InputStream in, OutputStream out) {
        run(new Tee(in, out));
    }

    private static void run(Tee target) {
        final Thread thread = new Thread(target);
        thread.setDaemon(true);
        thread.start();
    }

    public void run() {
        try {
            int i = -1;

            final byte[] buf = new byte[32*32*32];

            while ((i = in.read(buf)) != -1) {
                System.out.print("#");
                for (int o = 0; o < out.length; o++) {
                    try {
                        if (out[o] == null) continue;
                        out[o].write(buf, 0, i);
                    } catch (Exception e) {
                        new Exception(out[o].toString(), e).printStackTrace();
                        out[o] = null;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
