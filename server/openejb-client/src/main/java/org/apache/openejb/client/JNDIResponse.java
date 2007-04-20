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

public class JNDIResponse implements Response {

    private transient int responseCode = -1;
    private transient Object result;

    public JNDIResponse() {
    }

    public JNDIResponse(int code, Object obj) {
        responseCode = code;
        result = obj;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public Object getResult() {
        return result;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
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
                EJBMetaDataImpl m = new EJBMetaDataImpl();
                m.readExternal(in);
                result = m;
                break;
            case ResponseCodes.JNDI_DATA_SOURCE:
                DataSourceMetaData ds = new DataSourceMetaData();
                ds.readExternal(in);
                result = ds;
                break;
            case ResponseCodes.JNDI_INJECTIONS:
                InjectionMetaData imd = new InjectionMetaData();
                imd.readExternal(in);
                result = imd;
                break;
        }
    }

    public void writeExternal(ObjectOutput out) throws IOException {

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
                EJBMetaDataImpl m = (EJBMetaDataImpl) result;
                m.writeExternal(out);
                break;
            case ResponseCodes.JNDI_DATA_SOURCE:
                DataSourceMetaData ds = (DataSourceMetaData) result;
                ds.writeExternal(out);
                break;
            case ResponseCodes.JNDI_INJECTIONS:
                InjectionMetaData imd = (InjectionMetaData) result;
                imd.writeExternal(out);
                break;
        }
    }
}
