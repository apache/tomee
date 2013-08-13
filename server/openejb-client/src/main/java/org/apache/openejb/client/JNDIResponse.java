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

import javax.naming.Reference;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class JNDIResponse implements ClusterableResponse {

    private static final long serialVersionUID = 6741338056648918607L;
    private transient int responseCode = -1;
    private transient Object result;
    private transient ServerMetaData server;
    private transient JNDIRequest request;
    private transient ProtocolMetaData metaData;

    public JNDIResponse() {
    }

    public JNDIResponse(final int code, final Object obj) {
        responseCode = code;
        result = obj;
    }

    @Override
    public void setMetaData(final ProtocolMetaData metaData) {
        this.metaData = metaData;
    }

    public JNDIRequest getRequest() {
        return request;
    }

    public void setRequest(final JNDIRequest request) {
        this.request = request;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public Object getResult() {
        return result;
    }

    public void setResponseCode(final int responseCode) {
        this.responseCode = responseCode;
    }

    public void setResult(final Object result) {
        this.result = result;
    }

    @Override
    public void setServer(final ServerMetaData server) {
        this.server = server;
    }

    public ServerMetaData getServer() {
        return server;
    }

    @Override
    public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
        final byte version = in.readByte(); // future use

        final boolean readServer = in.readBoolean();
        if (readServer) {
            server = new ServerMetaData();
            server.setMetaData(metaData);
            server.readExternal(in);
        }

        responseCode = in.readByte();

        switch (responseCode) {
            case ResponseCodes.JNDI_BUSINESS_OBJECT:
            case ResponseCodes.JNDI_OK:
            case ResponseCodes.JNDI_NAMING_EXCEPTION:
            case ResponseCodes.JNDI_RUNTIME_EXCEPTION:
            case ResponseCodes.JNDI_ERROR:
            case ResponseCodes.JNDI_RESOURCE:
                result = in.readObject();
                break;
            case ResponseCodes.JNDI_CONTEXT:
            case ResponseCodes.JNDI_NOT_FOUND:
                break;
            case ResponseCodes.JNDI_EJBHOME:
                final EJBMetaDataImpl m = new EJBMetaDataImpl();
                m.setMetaData(metaData);
                m.readExternal(in);
                result = m;
                break;
            case ResponseCodes.JNDI_DATA_SOURCE:
                final DataSourceMetaData ds = new DataSourceMetaData();
                ds.setMetaData(metaData);
                ds.readExternal(in);
                result = ds;
                break;
            case ResponseCodes.JNDI_INJECTIONS:
                final InjectionMetaData imd = new InjectionMetaData();
                imd.setMetaData(metaData);
                imd.readExternal(in);
                result = imd;
                break;
            case ResponseCodes.JNDI_WEBSERVICE:
                result = in.readObject();
                break;
            case ResponseCodes.JNDI_ENUMERATION:
                final NameClassPairEnumeration ncpe = new NameClassPairEnumeration();
                ncpe.setMetaData(metaData);
                ncpe.readExternal(in);
                result = ncpe;
                break;
            case ResponseCodes.JNDI_REFERENCE:
                result = in.readObject();
                break;
        }
    }

    @Override
    public void writeExternal(final ObjectOutput out) throws IOException {
        // write out the version of the serialized data for future use
        out.writeByte(1);

        if (null != server) {
            out.writeBoolean(true);
            server.setMetaData(metaData);
            server.writeExternal(out);
        } else {
            out.writeBoolean(false);
        }

        out.writeByte((byte) responseCode);

        switch (responseCode) {
            case ResponseCodes.JNDI_BUSINESS_OBJECT:
            case ResponseCodes.JNDI_OK:
            case ResponseCodes.JNDI_NAMING_EXCEPTION:
            case ResponseCodes.JNDI_RUNTIME_EXCEPTION:
            case ResponseCodes.JNDI_ERROR:
            case ResponseCodes.JNDI_RESOURCE:
                out.writeObject(result);
                break;
            case ResponseCodes.JNDI_CONTEXT:
            case ResponseCodes.JNDI_NOT_FOUND:
                break;
            case ResponseCodes.JNDI_EJBHOME:
                final EJBMetaDataImpl m = (EJBMetaDataImpl) result;
                m.setMetaData(metaData);
                m.writeExternal(out);
                break;
            case ResponseCodes.JNDI_DATA_SOURCE:
                final DataSourceMetaData ds = (DataSourceMetaData) result;
                ds.setMetaData(metaData);
                ds.writeExternal(out);
                break;
            case ResponseCodes.JNDI_INJECTIONS:
                final InjectionMetaData imd = (InjectionMetaData) result;
                imd.setMetaData(metaData);
                imd.writeExternal(out);
                break;
            case ResponseCodes.JNDI_WEBSERVICE:
                final WsMetaData ws = (WsMetaData) result;
                out.writeObject(ws);
                break;
            case ResponseCodes.JNDI_ENUMERATION:
                final NameClassPairEnumeration ncpe = (NameClassPairEnumeration) result;
                ncpe.setMetaData(metaData);
                ncpe.writeExternal(out);
                break;
            case ResponseCodes.JNDI_REFERENCE:
                final Reference ref = (Reference) result;
                out.writeObject(ref);
                break;
        }
    }

    public String toString() {
        final StringBuilder sb = new StringBuilder(100);

        switch (responseCode) {
            case ResponseCodes.JNDI_BUSINESS_OBJECT:
                sb.append("JNDI_BUSINESS_OBJECT:");
                break;
            case ResponseCodes.JNDI_OK:
                sb.append("JNDI_OK:");
                break;
            case ResponseCodes.JNDI_NAMING_EXCEPTION:
                sb.append("JNDI_NAMING_EXCEPTION:");
                break;
            case ResponseCodes.JNDI_RUNTIME_EXCEPTION:
                sb.append("JNDI_RUNTIME_EXCEPTION:");
                break;
            case ResponseCodes.JNDI_ERROR:
                sb.append("JNDI_ERROR:");
                break;
            case ResponseCodes.JNDI_RESOURCE:
                sb.append("JNDI_RESOURCE:");
                break;
            case ResponseCodes.JNDI_CONTEXT:
                sb.append("JNDI_CONTEXT:");
                break;
            case ResponseCodes.JNDI_NOT_FOUND:
                sb.append("JNDI_NOT_FOUND:");
                break;
            case ResponseCodes.JNDI_EJBHOME:
                sb.append("JNDI_EJBHOME:");
                break;
            case ResponseCodes.JNDI_DATA_SOURCE:
                sb.append("JNDI_DATA_SOURCE:");
                break;
            case ResponseCodes.JNDI_INJECTIONS:
                sb.append("JNDI_INJECTIONS:");
                break;
        }
        sb.append(this.getResult());
        return sb.toString();
    }

}
