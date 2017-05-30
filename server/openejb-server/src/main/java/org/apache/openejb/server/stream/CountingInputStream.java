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
package org.apache.openejb.server.stream;

import java.io.IOException;
import java.io.InputStream;

public class CountingInputStream extends InputStream {
    private final InputStream delegate;
    private int count = 0;

    public CountingInputStream(final InputStream rawIn) {
        delegate = rawIn;
    }

    @Override
    public int read() throws IOException {
        final int r = delegate.read();
        if (r == -1) {
            return -1;
        }

        count++;
        return r;
    }

    @Override
    public int read(final byte[] b) throws IOException {
        final int read = delegate.read(b);
        count += read;
        return read;
    }

    @Override
    public int read(final byte[] b, final int off, final int len) throws IOException {
        final int read = delegate.read(b, off, len);
        count += read;
        return read;
    }

    @Override
    public long skip(final long n) throws IOException {
        final long skip = delegate.skip(n);
        count += skip;
        return skip;
    }

    @Override
    public int available() throws IOException {
        return delegate.available();
    }

    @Override
    public void close() throws IOException {
        delegate.close();
    }

    @Override
    public void mark(final int readlimit) {
        delegate.mark(readlimit);
    }

    @Override
    public void reset() throws IOException {
        delegate.reset();
    }

    @Override
    public boolean markSupported() {
        return delegate.markSupported();
    }

    public int getCount() {
        return count;
    }
}
