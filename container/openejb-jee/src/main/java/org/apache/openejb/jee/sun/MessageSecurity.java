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
package org.apache.openejb.jee.sun;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "message",
    "requestProtection",
    "responseProtection"
})
public class MessageSecurity {
    @XmlElement(required = true)
    protected List<Message> message;
    @XmlElement(name = "request-protection")
    protected RequestProtection requestProtection;
    @XmlElement(name = "response-protection")
    protected ResponseProtection responseProtection;

    public List<Message> getMessage() {
        if (message == null) {
            message = new ArrayList<Message>();
        }
        return this.message;
    }

    public RequestProtection getRequestProtection() {
        return requestProtection;
    }

    public void setRequestProtection(RequestProtection value) {
        this.requestProtection = value;
    }

    public ResponseProtection getResponseProtection() {
        return responseProtection;
    }

    public void setResponseProtection(ResponseProtection value) {
        this.responseProtection = value;
    }
}
