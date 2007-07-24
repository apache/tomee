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

public class EJBResponse implements Response {

    private transient int responseCode = -1;
    private transient Object result;

    public EJBResponse() {

    }

    public EJBResponse(int code, Object obj) {
        responseCode = code;
        result = obj;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public Object getResult() {
        return result;
    }

    public void setResponse(int code, Object result) {
        this.responseCode = code;
        this.result = result;
    }

    public String toString() {
        StringBuffer s = null;
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
        s.append(':').append(result);

        return s.toString();
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        byte version = in.readByte(); // future use

        responseCode = in.readByte();

        result = in.readObject();
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        // write out the version of the serialized data for future use
        out.writeByte(1);

        out.writeByte(responseCode);

        switch (responseCode) {
            case ResponseCodes.EJB_APP_EXCEPTION:
            case ResponseCodes.EJB_ERROR:
            case ResponseCodes.EJB_SYS_EXCEPTION:
                if (result instanceof Throwable && !(result instanceof ThrowableArtifact)) {
                    Throwable throwable = (Throwable) result;
                    result = new ThrowableArtifact(throwable);
                }
        }
        out.writeObject(result);
    }
}
