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
package org.apache.openjpa.persistence.meta.common.apps;

import java.io.Serializable;
import javax.persistence.Entity;

import org.apache.openjpa.meta.ClassMetaData;

@Entity
public class NonPersistentFieldsPC {

    private String persistentField;
    private String nonPersistentField;
    private Object objectField;
    private Serializable interfaceField;
    private ClassMetaData userObjectField;
    private ClassMetaData userInterfaceField;
    private Object explicitObjectField;
    private Serializable explicitInterfaceField;
    private ClassMetaData explicitUserObjectField;
    private ClassMetaData explicitUserInterfaceField;
    private Object persistentObjectField;
    private Serializable persistentInterfaceField;
    private ClassMetaData persistentUserObjectField;
    private ClassMetaData persistentUserInterfaceField;
}
