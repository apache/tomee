/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */
package org.apache.openjpa.jdbc.meta.strats;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * This class is used to kwon where the content of the InputStream is load.
 * If the content is load out of the flush then throws a
 * UnsupportedOperationException
 *
 * @author Ignacio Andreu
 * @since 1.1.0
 */

public class InputStreamWrapper extends InputStream {

    private InputStream is;

    public InputStreamWrapper(String s) {
        this.is = new ByteArrayInputStream(s.getBytes());
    }

    public int read() throws IOException {
        throw new UnsupportedOperationException();
    }

    public int available() throws IOException {
        return is.available();
    }

    public void close() throws IOException {
        is.close();
    }

    public int read(byte[] b, int off, int len) throws IOException {
        StackTraceElement[] ste = Thread.currentThread().getStackTrace();
        for (StackTraceElement element : ste) {
            if ("flush".equals(element.getMethodName())) {
                return is.read(b, off, len);
            }
        }
        throw new UnsupportedOperationException();
    }

    public int read(byte[] b) throws IOException {
        throw new UnsupportedOperationException();
    }
}
