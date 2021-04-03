/*
 *     Licensed to the Apache Software Foundation (ASF) under one or more
 *     contributor license agreements.  See the NOTICE file distributed with
 *     this work for additional information regarding copyright ownership.
 *     The ASF licenses this file to You under the Apache License, Version 2.0
 *     (the "License"); you may not use this file except in compliance with
 *     the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package org.apache.openejb.server.cxf.rs;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

public class Logs {
    public static String forceLength(final String httpMethod, final int l, final boolean right) {
        final String http;
        if (httpMethod == null) { // subresourcelocator implies null http method
            http = "";
        } else {
            http = httpMethod;
        }

        final StringBuilder builder = new StringBuilder();
        if (!right) {
            for (int i = 0; i < l - http.length(); i++) {
                builder.append(" ");
            }
        }
        builder.append(http);
        if (right) {
            for (int i = 0; i < l - http.length(); i++) {
                builder.append(" ");
            }
        }
        return builder.toString();
    }

    public static String toSimpleString(final Method mtd) {
        try {
            final StringBuilder sb = new StringBuilder();
            final Type[] typeparms = mtd.getTypeParameters();
            if (typeparms.length > 0) {
                boolean first = true;
                sb.append("<");
                for (Type typeparm : typeparms) {
                    if (!first) {
                        sb.append(",");
                    }
                    sb.append(name(typeparm));
                    first = false;
                }
                sb.append("> ");
            }

            final Type genRetType = mtd.getGenericReturnType();
            sb.append(name(genRetType)).append(" ");
            sb.append(mtd.getName()).append("(");
            final Type[] params = mtd.getGenericParameterTypes();
            for (int j = 0; j < params.length; j++) {
                sb.append(name(params[j]));
                if (j < (params.length - 1)) {
                    sb.append(", ");
                }
            }
            sb.append(")");
            final Type[] exceptions = mtd.getGenericExceptionTypes();
            if (exceptions.length > 0) {
                sb.append(" throws ");
                for (int k = 0; k < exceptions.length; k++) {
                    sb.append(name(exceptions[k]));
                    if (k < (exceptions.length - 1)) {
                        sb.append(",");
                    }
                }
            }
            return sb.toString();
        } catch (Exception e) {
            return "<" + e + ">";
        }
    }

    public static String name(final Type type) {
        if (type instanceof Class<?>) {
            return ((Class) type).getSimpleName().replace("java.lang.", "").replace("java.util", "");
        } else if (type instanceof ParameterizedType) {
            final ParameterizedType pt = (ParameterizedType) type;
            final StringBuilder builder = new StringBuilder();
            builder.append(name(pt.getRawType()));
            final Type[] args = pt.getActualTypeArguments();
            if (args != null) {
                builder.append("<");
                for (int i = 0; i < args.length; i++) {
                    builder.append(name(args[i]));
                    if (i < args.length - 1) {
                        builder.append(", ");
                    }
                }
                builder.append(">");
            }
            return builder.toString();
        }
        return type.toString();
    }

    public static String singleSlash(final String address, final String value) {
        if (address.endsWith("/") && value.startsWith("/")) {
            return address + value.substring(1);
        }
        if (!address.endsWith("/") && !value.startsWith("/")) {
            return address + '/' + value;
        }
        if (value.equals("/")) {
            return address;
        }
        return address + value;
    }

    public static class LogOperationEndpointInfo implements Comparable<LogOperationEndpointInfo> {
        public final String http;
        public final String address;
        public final String method;

        public LogOperationEndpointInfo(final String http, final String address, final String method) {
            this.address = address;
            this.method = method;

            if (http != null) {
                this.http = http;
            } else { // can happen with subresource locators
                this.http = "";
            }
        }

        @Override
        public int compareTo(final LogOperationEndpointInfo o) {
            int compare = http.compareTo(o.http);
            if (compare != 0) {
                return compare;
            }

            compare = address.compareTo(o.address);
            if (compare != 0) {
                return compare;
            }

            return method.compareTo(o.method);
        }

        @Override
        public String toString() {
            return "LogOperationEndpointInfo{" +
                    "http='" + http + '\'' +
                    ", address='" + address + '\'' +
                    ", method='" + method + '\'' +
                    '}';
        }
    }

    public static class LogResourceEndpointInfo implements Comparable<LogResourceEndpointInfo> {
        public final String type;
        public final String address;
        public final String classname;
        public final List<LogOperationEndpointInfo> operations;
        public final int methodSize;
        public final int methodStrSize;

        public LogResourceEndpointInfo(final String type, final String address, final String classname,
                                       final List<LogOperationEndpointInfo> operations,
                                       final int methodSize, final int methodStrSize) {
            this.type = type;
            this.address = address;
            this.classname = classname;
            this.operations = operations;
            this.methodSize = methodSize;
            this.methodStrSize = methodStrSize;
        }

        @Override
        public int compareTo(final LogResourceEndpointInfo o) {
            int compare = type.compareTo(o.type);
            if (compare != 0) {
                return compare;
            }

            compare = address.compareTo(o.address);
            if (compare != 0) {
                return compare;
            }

            return classname.compareTo(o.classname);
        }
    }
}
