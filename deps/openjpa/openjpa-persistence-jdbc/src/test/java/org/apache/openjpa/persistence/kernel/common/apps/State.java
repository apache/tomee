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
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import org.apache.openjpa.persistence.FetchGroup;
import org.apache.openjpa.persistence.FetchAttribute;
import org.apache.openjpa.persistence.FetchGroups;
import org.apache.openjpa.persistence.kernel.TestIndirectRecursion;

/**
 * Represents a graph node.
 * Used in testing {@linkplain TestIndirectRecursion}.
 * 
 * @author Pinaki Poddar
 *
 */
@SuppressWarnings("serial")
@Entity
@FetchGroups( {
        @FetchGroup(name = "State_OutgoingTransitions", attributes = { @FetchAttribute(name = "outgoingTransitions") }),
        @FetchGroup(name = "State_IncomingTransitions", attributes = { @FetchAttribute(name = "incomingTransitions")})})
public class State implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(length = 64)
    private String name;

    @OneToMany(mappedBy = "fromState", cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH })
    private List<Transition> outgoingTransitions;

    @OneToMany(mappedBy = "toState", cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH })
    private List<Transition> incomingTransitions;

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Transition> getOutgoingTransitions() {
        return outgoingTransitions;
    }

    public void addOutgoingTransitions(Transition outgoingTransition) {
        if (outgoingTransitions == null)
            outgoingTransitions = new ArrayList<Transition>();
        outgoingTransitions.add(outgoingTransition);
    }

    public List<Transition> getIncomingTransitions() {
        return incomingTransitions;
    }

    public void addIncomingTransitions(Transition incomingTransition) {
        if (incomingTransitions == null)
            incomingTransitions = new ArrayList<Transition>();
        incomingTransitions.add(incomingTransition);
    }
}
