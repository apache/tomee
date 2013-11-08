/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */
package hellojpa;

import java.util.*;
import javax.persistence.*;


/** 
 * A very simple persistent entity that holds a "message", has a
 * "created" field that is initialized to the time at which the
 * object was created, and an id field that is initialized to the
 * current time.
 */
@Entity
public class Message {
    @Id
    private long id = System.currentTimeMillis();

    @Basic
    private String message;

    @Basic
    private Date created = new Date();

    public Message() {
    }

    public Message(String msg) {
        message = msg;
    }

    public void setId(long val) {
        id = val;
    }

    public long getId() {
        return id;
    }

    public void setMessage(String msg) {
        message = msg;
    }

    public String getMessage() {
        return message;
    }

    public void setCreated(Date date) {
        created = date;
    }

    public Date getCreated() {
        return created;
    }
}
