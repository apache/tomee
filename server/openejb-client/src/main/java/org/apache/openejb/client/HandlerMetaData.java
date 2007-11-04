/**
 *
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

import java.util.List;
import java.util.ArrayList;
import java.io.Serializable;

public class HandlerMetaData implements Serializable {
    private static final long serialVersionUID = -297817668220375028L;
    private String handlerClass;
    private List<CallbackMetaData> postConstruct = new ArrayList<CallbackMetaData>();
    private List<CallbackMetaData> preDestroy = new ArrayList<CallbackMetaData>();

    public String getHandlerClass() {
        return handlerClass;
    }

    public void setHandlerClass(String handlerClass) {
        this.handlerClass = handlerClass;
    }

    public List<CallbackMetaData> getPostConstruct() {
        return postConstruct;
    }

    public void setPostConstruct(List<CallbackMetaData> postConstruct) {
        this.postConstruct = postConstruct;
    }

    public List<CallbackMetaData> getPreDestroy() {
        return preDestroy;
    }

    public void setPreDestroy(List<CallbackMetaData> preDestroy) {
        this.preDestroy = preDestroy;
    }
}
