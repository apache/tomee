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
package org.apache.openejb.client;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class EJBResponse implements ClusterableResponse {

    /**
     * 1. Initial
     * 2. Append times.
     */
    public static final byte VERSION = 2;

    private transient byte version = VERSION;
    private transient int responseCode = -1;
    private transient Object result;
    private transient ServerMetaData server;
    private transient final long[] times = new long[Time.values().length];
    private transient final int timesLength = times.length;

    public EJBResponse() {
    }

    public int getResponseCode() {
        return responseCode;
    }

    public Object getResult() {
        return result;
    }

    public void setResponse(final byte version, final int code, final Object result) {
        this.version = version;
        this.responseCode = code;
        this.result = result;
    }

    @Override
    public void setServer(final ServerMetaData server) {
        this.server = server;
    }

    public ServerMetaData getServer() {
        return server;
    }

    public String toString() {

        final StringBuffer s;

        switch (responseCode) {
            case ResponseCodes.EJB_APP_EXCEPTION:
                s = new StringBuffer("EJB_APP_EXCEPTION");
                break;
            case ResponseCodes.EJB_ERROR:
                s = new StringBuffer("EJB_ERROR");
                break;
            case ResponseCodes.EJB_OK:
                s = new StringBuffer("EJB_OK");
                break;
            case ResponseCodes.EJB_OK_CREATE:
                s = new StringBuffer("EJB_OK_CREATE");
                break;
            case ResponseCodes.EJB_OK_FOUND:
                s = new StringBuffer("EJB_OK_FOUND");
                break;
            case ResponseCodes.EJB_OK_FOUND_COLLECTION:
                s = new StringBuffer("EJB_OK_FOUND_COLLECTION");
                break;
            case ResponseCodes.EJB_OK_FOUND_ENUMERATION:
                s = new StringBuffer("EJB_OK_FOUND_ENUMERATION");
                break;
            case ResponseCodes.EJB_OK_NOT_FOUND:
                s = new StringBuffer("EJB_OK_NOT_FOUND");
                break;
            case ResponseCodes.EJB_SYS_EXCEPTION:
                s = new StringBuffer("EJB_SYS_EXCEPTION");
                break;
            default:
                s = new StringBuffer("UNKNOWN_RESPONSE");
        }

        s.append(", serverTime=").append(times[Time.TOTAL.ordinal()]).append("ns");
        s.append(", containerTime").append(times[Time.CONTAINER.ordinal()]).append("ns");
        s.append(" : ").append(result);

        return s.toString();
    }

    public void start(final EJBResponse.Time time) {
        times[time.ordinal()] = System.nanoTime();
    }

    public void stop(final EJBResponse.Time time) {
        times[time.ordinal()] = System.nanoTime() - times[time.ordinal()];
    }

    @Override
    public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {

        final byte version = in.readByte();

        final boolean readServer = in.readBoolean();
        if (readServer) {
            server = new ServerMetaData();
            server.readExternal(in);
        }

        responseCode = in.readByte();

        result = in.readObject();

        if (version == 2) {

            final byte size = in.readByte();

            for (int i = 0; (i < size && i < timesLength); i++) {
                times[i] = in.readLong();
            }
        }
    }

    @Override
    public void writeExternal(final ObjectOutput out) throws IOException {

        out.writeByte(this.version);

        if (null != server) {
            out.writeBoolean(true);
            server.writeExternal(out);
        } else {
            out.writeBoolean(false);
        }

        out.writeByte(responseCode);

        switch (responseCode) {
            case ResponseCodes.EJB_APP_EXCEPTION:
            case ResponseCodes.EJB_ERROR:
            case ResponseCodes.EJB_SYS_EXCEPTION:
                if (result instanceof Throwable) {
                    final Throwable throwable = (Throwable) result;
                    result = new ThrowableArtifact(throwable);
                }
                break;
        }

        start(Time.SERIALIZATION);
        out.writeObject(result);
        stop(Time.SERIALIZATION);
        stop(Time.TOTAL);

        if (this.version == 2) {

            out.writeByte(timesLength);

            for (final long time : times) {
                out.writeLong(time);
            }
        }
    }

    public static enum Time {
        TOTAL,
        CONTAINER,
        SERIALIZATION,
        DESERIALIZATION
    }
}
