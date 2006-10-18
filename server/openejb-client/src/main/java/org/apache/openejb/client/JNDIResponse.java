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
            case JNDI_BUSINESS_OBJECT:
            case JNDI_OK:
            case JNDI_NAMING_EXCEPTION:
            case JNDI_RUNTIME_EXCEPTION:
            case JNDI_ERROR:
                result = in.readObject();
                break;
            case JNDI_CONTEXT:
            case JNDI_NOT_FOUND:
                break;
            case JNDI_EJBHOME:
                EJBMetaDataImpl m = new EJBMetaDataImpl();
                m.readExternal(in);
                result = m;
                break;
        }
    }

    public void writeExternal(ObjectOutput out) throws IOException {

        out.writeByte((byte) responseCode);

        switch (responseCode) {
            case JNDI_BUSINESS_OBJECT:
            case JNDI_OK:
            case JNDI_NAMING_EXCEPTION:
            case JNDI_RUNTIME_EXCEPTION:
            case JNDI_ERROR:
                out.writeObject(result);
                break;
            case JNDI_CONTEXT:
            case JNDI_NOT_FOUND:
                break;
            case JNDI_EJBHOME:
                EJBMetaDataImpl m = (EJBMetaDataImpl) result;
                m.writeExternal(out);
                break;

        }
    }
}
