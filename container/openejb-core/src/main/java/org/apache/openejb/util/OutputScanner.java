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
package org.apache.openejb.util;

import org.codehaus.swizzle.stream.ScanBuffer;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @version $Rev$ $Date$
 */
public class OutputScanner extends FilterOutputStream {

    private final CountDownLatch found = new CountDownLatch(1);

    public OutputScanner(OutputStream out, String scanString) {
        super(null);
        this.out = new Scan(out, scanString);
    }

    public class Scan extends FilterOutputStream {

        private final ScanBuffer scan;

        public Scan(OutputStream out, String scanString) {
            super(out);
            scan = new ScanBuffer(scanString);
        }

        @Override
        public void write(int b) throws IOException {
            check(b);
            super.write(b);
        }

        @Override
        public void write(byte[] bytes) throws IOException {
            for (byte b : bytes) {
                check(b);
            }
            super.write(bytes);
        }

        @Override
        public void write(byte[] bytes, int off, int len) throws IOException {
            for (int i = off; i < len; i++) {
                check(bytes[i]);
            }
            super.write(bytes, off, len);
        }

        private void check(int b) {
            scan.append(b);
            if (scan.match()) {
                // Cut ourselves out of the call chain.
                //
                // This works because
                //  - ScanningOutputStreamFilter.this.out == this
                //  - this.out != this)
                //
                // Our parent is delegating to us and we are delegating
                // to the actual OutputStream
                //
                // To cut ourselves out of the call chain and eliminate
                // the overhead of checking the ScanBuffer, we set our
                // parent to not delegate to us and to instead delegate
                // to the actual OutputStream.

                // Intellij mistakenly shows this grayed out,
                // however it is very very significant.
                OutputScanner.this.out = this.out;
                found.countDown();
            }
        }
    }


    public void await() throws InterruptedException {
        found.await();
    }

    public boolean await(long timeout, TimeUnit unit) throws InterruptedException {
        return found.await(timeout, unit);
    }
}
