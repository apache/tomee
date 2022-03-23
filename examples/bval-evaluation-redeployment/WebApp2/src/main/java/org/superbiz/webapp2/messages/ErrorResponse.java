/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.superbiz.webapp2.messages;

import jakarta.ws.rs.core.Response;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.Date;

@XmlRootElement
public class ErrorResponse implements Serializable {
    private static final long serialVersionUID = 8888101217538645771L;

    private Long id;
    private Response.Status status;
    private String message;

    public ErrorResponse() {
        this.id = new Date().getTime();
    }

    public ErrorResponse(final Response.Status status, final String message) {
        this.id = new Date().getTime();
        this.status = status;
        this.message = message;
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public Response.Status getStatus() {
        return status;
    }

    @XmlAttribute
    public void setStatus(final Response.Status status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    @XmlAttribute
    public void setMessage(final String message) {
        this.message = message;
    }

    //    @Override
//    public String toString() {
//        return "{" + "id:" + id + ", status:" + status + ", message:" + message + '}';
//    }
    @Override
    public String toString() {
        return "ErrorResponse:" + "id=" + id + ", status=" + status + ", message=" + message;
    }
}
