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
package org.apache.openjpa.persistence.inheritance.entity;

import javax.persistence.Basic;
import javax.persistence.Entity;

@Entity
public class SubclassF extends BaseClass3 implements ManagedIface2  {

    @Basic
    private String classFName;
        
    @Basic
    private int intFieldSup;
    
    public void setClassFName(String classFName) {
        this.classFName = classFName;
    }

    public String getClassFName() {
        return classFName;
    }
    
    public String toString() {
        return super.toString() + ";classFName=" + classFName + 
            ";intFieldSup=" + intFieldSup;
    }

    public int getIntFieldSup() {
        return intFieldSup;
    }

    public void setIntFieldSup(int i) {
        this.intFieldSup = i;
    }
}
