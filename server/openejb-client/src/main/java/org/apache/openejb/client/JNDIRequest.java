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

public class JNDIRequest implements Request {

    private transient int requestMethod = -1;
    private transient String requestString;

    public JNDIRequest() {
    }

    public JNDIRequest(int requestMethod, String requestString) {
        this.requestMethod = requestMethod;
        this.requestString = requestString;
    }

    public byte getRequestType() {
        return JNDI_REQUEST;
    }

    public int getRequestMethod() {
        return requestMethod;
    }

    public String getRequestString() {
        return requestString;
    }

    public void setRequestMethod(int requestMethod) {
        this.requestMethod = requestMethod;
    }

    public void setRequestString(String requestString) {
        this.requestString = requestString;
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        requestMethod = in.readByte();
        requestString = in.readUTF();
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeByte((byte) requestMethod);
        out.writeUTF(requestString);
    }

}

