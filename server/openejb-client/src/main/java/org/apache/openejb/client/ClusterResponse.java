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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.client;

import java.io.ObjectInput;
import java.io.IOException;
import java.io.ObjectOutput;

/**
 * @version $Rev$ $Date$
 */
public class ClusterResponse implements Response {
    public static enum Code {
        CURRENT, UPDATE, FAILURE;
    }

    private Code responseCode;
    private ClusterMetaData updatedMetaData;
    private Throwable failure;

    public ClusterResponse(Code responseCode) {
        this.responseCode = responseCode;
    }

    public ClusterResponse() {
    }

    public Code getResponseCode() {
        return responseCode;
    }

    public void setCurrent() {
        this.responseCode = Code.CURRENT;
    }

    public void setUpdatedMetaData(ClusterMetaData updatedMetaData) {
        this.responseCode = Code.UPDATE;
        this.updatedMetaData = updatedMetaData;
    }

    public ClusterMetaData getUpdatedMetaData() {
        return updatedMetaData;
    }

    public Throwable getFailure() {
        return failure;
    }

    public void setFailure(Throwable failure) {
        this.responseCode = Code.FAILURE;
        this.failure = failure;
    }


    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        byte i = in.readByte();
        responseCode = Code.values()[i];

        switch(responseCode){
            case CURRENT: break;
            case UPDATE: {
                updatedMetaData = new ClusterMetaData();
                updatedMetaData.readExternal(in);
            }; break;
            case FAILURE:{
                failure = (IOException) in.readObject();
            }
        }
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeByte(responseCode.ordinal());

        switch(responseCode){
            case CURRENT: break;
            case UPDATE: {
                updatedMetaData.writeExternal(out);
            }; break;
            case FAILURE:{
                out.writeObject(failure);
            }
        }
    }
}
