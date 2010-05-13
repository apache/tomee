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

import java.io.InputStream;
import java.io.OutputStream;

/**
 * @version $Rev$ $Date$
 */
public final class Pipe implements Runnable {

    private final InputStream in;
    private final OutputStream out;

    public Pipe(InputStream in, OutputStream out) {
        this.in = in;
        this.out = out;
    }

    public static void pipe(Process process) {
        pipe(process.getInputStream(), System.out);
        pipe(process.getErrorStream(), System.err);
        pipe(System.in, process.getOutputStream());
    }

    public static void pipe(InputStream in, OutputStream out) {
        final Thread thread = new Thread(new Pipe(in, out));
        thread.setDaemon(true);
        thread.start();
    }

    public void run() {
        try {
            int i = -1;

            byte[] buf = new byte[1024];

            while ((i = in.read(buf)) != -1) {
                out.write(buf, 0, i);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
