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
package org.apache.openejb.server.context;

import org.apache.openejb.server.stream.CountingInputStream;
import org.apache.openejb.server.stream.CountingOutputStream;

import jakarta.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

public final class RequestInfos {

    private static final ThreadLocal<RequestInfo> REQUEST_INFO = new ThreadLocal<>();

    private RequestInfos() {
        // no-op
    }

    public static void initRequestInfo(final HttpServletRequest request) {
        final RequestInfo value = forceRequestInfo();
        value.ip = request.getRemoteAddr();
        REQUEST_INFO.set(value);
    }

    public static void initRequestInfo(final Socket socket) {
        final RequestInfo value = forceRequestInfo();
        final SocketAddress remoteSocketAddress = socket.getRemoteSocketAddress();
        if (remoteSocketAddress != null && InetSocketAddress.class.isInstance(remoteSocketAddress)) {
            final InetSocketAddress socketAddress = InetSocketAddress.class.cast(remoteSocketAddress);
            final InetAddress address = socketAddress.getAddress();
            if (address != null) {
                value.ip = address.getHostAddress();
            } else {
                value.ip = socketAddress.getHostName();
            }
        }
    }

    private static RequestInfo forceRequestInfo() {
        RequestInfo value = REQUEST_INFO.get();
        if (value == null) {
            value = new RequestInfo();
            REQUEST_INFO.set(value);
        }
        return value;
    }

    public static void clearRequestInfo() {
        REQUEST_INFO.remove();
    }

    public static RequestInfo info() {
        return REQUEST_INFO.get();
    }

    public static class RequestInfo {

        public String ip;
        private InputStream inputStream;
        private OutputStream outputStream;

        public InputStream getInputStream() {
            if (null == inputStream) {
                throw new RuntimeException("InputStream has not been set");
            }

            return inputStream;
        }

        public OutputStream getOutputStream() {

            if (null == outputStream) {
                throw new RuntimeException("OutputStream has not been set");
            }

            return outputStream;
        }

        public void setInputStream(final InputStream inputStream) {
            this.inputStream = inputStream;
        }

        public void setOutputStream(final OutputStream outputStream) {
            this.outputStream = outputStream;
        }

        @Override
        public String toString() {
            return "RequestInfo{"
                + "ip='" + ip + '\''
                + ", request-size=" + (inputStream != null && CountingInputStream.class.isInstance(inputStream) ?
                    CountingInputStream.class.cast(inputStream).getCount() : "unknown")
                + ", response-size=" + (outputStream != null && CountingOutputStream.class.isInstance(outputStream) ?
                    CountingOutputStream.class.cast(outputStream).getCount() : "unknown")
                + '}';
        }
    }
}
