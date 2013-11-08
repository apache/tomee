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
package org.apache.openjpa.persistence.kernel.common.apps;

import java.io.Serializable;
import javax.persistence.Entity;

import org.apache.openjpa.persistence.Persistent;
import org.apache.openjpa.persistence.Type;

/**
 * <p>Persistent type used in testing.</p>
 *
 * @author Abe White
 */
@Entity
public class PCClassPC
    implements PCClassInterface, Serializable {

    @Persistent
    @Type(PCClassPC.class)
    private Object specificPC;

    @Persistent
    @Type(Entity.class)
    private Object genericPC;

    @Persistent
    private Object genericObject;

    @Persistent
    @Type(PCClassPC.class)
    private PCClassInterface specificInterface;

    @Persistent
    private PCClassInterface defaultInterface;

    @Persistent
    private Serializable serializableInterface;

    @Persistent
    @Type(Object.class)
    private PCClassInterface genericInterface;

    public Object getSpecificPC() {
        return this.specificPC;
    }

    public void setSpecificPC(Object specificPC) {
        this.specificPC = specificPC;
    }

    public Object getGenericPC() {
        return this.genericPC;
    }

    public void setGenericPC(Object genericPC) {
        this.genericPC = genericPC;
    }

    public Object getGenericObject() {
        return this.genericObject;
    }

    public void setGenericObject(Object genericObject) {
        this.genericObject = genericObject;
    }

    public PCClassInterface getSpecificInterface() {
        return this.specificInterface;
    }

    public void setSpecificInterface(PCClassInterface specificInterface) {
        this.specificInterface = specificInterface;
    }

    public PCClassInterface getDefaultInterface() {
        return this.defaultInterface;
    }

    public void setDefaultInterface(PCClassInterface defaultInterface) {
        this.defaultInterface = defaultInterface;
    }

    public Serializable getSerializableInterface() {
        return this.serializableInterface;
    }

    public void setSerializableInterface(Serializable serializableInterface) {
        this.serializableInterface = serializableInterface;
    }

    public PCClassInterface getGenericInterface() {
        return this.genericInterface;
    }

    public void setGenericInterface(PCClassInterface genericInterface) {
        this.genericInterface = genericInterface;
    }
}
