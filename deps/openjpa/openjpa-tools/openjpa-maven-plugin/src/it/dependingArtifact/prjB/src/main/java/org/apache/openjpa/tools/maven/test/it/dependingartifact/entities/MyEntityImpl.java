package org.apache.openjpa.tools.maven.test.it.dependingartifact.entities;
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


import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.apache.openjpa.tools.maven.test.it.dependingartifact.MyEntityInterface;

/**
 * This class implements an interface and references to an enum of that interface.
 * This causes the PCEnhancer of OpenJPA-1.2.1 to demand the interface on  
 * the classpath.
 */
@Entity
public class MyEntityImpl implements MyEntityInterface {

    @Id
    @GeneratedValue
    private int id;
    
    private MessageChannel messageChannel;
    
    
    public int getId() {
        return id;
    }

    public void setId( int id ) {
        this.id = id;
    }

    public MessageChannel getMessageChannel() 
    {
        return messageChannel;
    }

    public void setMessageChannel( MessageChannel messageChannel ) 
    {
        this.messageChannel = messageChannel;
    }

}
