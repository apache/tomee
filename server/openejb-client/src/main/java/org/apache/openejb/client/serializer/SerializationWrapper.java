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
package org.apache.openejb.client.serializer;

import java.io.Serializable;

public class SerializationWrapper implements Serializable {

    private static final long serialVersionUID = -9108946890164480879L;
    private String classname;
    private Serializable data;

    public SerializationWrapper(final Serializable serialize, final String name) {
        data = serialize;
        classname = name;
    }

    public String getClassname() {
        return classname;
    }

    public void setClassname(final String classname) {
        this.classname = classname;
    }

    public Serializable getData() {
        return data;
    }

    public void setData(final Serializable data) {
        this.data = data;
    }
}
