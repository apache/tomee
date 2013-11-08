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
package org.apache.openjpa.persistence.jdbc.common.apps;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.PersistenceException;

@Entity
public class EntityWithFailedExternalizer implements Serializable {

    private static final long serialVersionUID = 1L;
    
    @Id
    private int iref;

    private String name;

    private String data;

    @org.apache.openjpa.persistence.Persistent
    @org.apache.openjpa.persistence.Externalizer("check")
    private TestExternal ext;
    
    public static class TestExternal
    {
        private static final long serialVersionUID = 1L;
        public boolean throwEx=false;
        
        private String value = "test - TE";

        public TestExternal() {
            super();
        }
        
        public TestExternal(String s) {
            value = s;
        }
        
        public String check() throws Exception {
            if (throwEx){
                throw new PersistenceException("test exception externalizer");
            }
            return value;           
        }
        
        public String getValue() {
            return value;
        }
        
        public void getValue(String s) {
            value = s;
        }
    }

    public EntityWithFailedExternalizer() {
        super();
    }

    public EntityWithFailedExternalizer(int iref, String name, String data) {
        super();
        this.iref = iref;
        this.name = name;
        this.data = data;
        this.ext = new TestExternal();
    }
    
    public int getIref() {
        return this.iref;
    }

    public void setIref(int iref) {
        this.iref = iref;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getData() {
        return this.data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public void setExt(TestExternal te){
        this.ext = te;
        return;
    }
    
    public TestExternal getExt(){
        return this.ext;
    }   
}

