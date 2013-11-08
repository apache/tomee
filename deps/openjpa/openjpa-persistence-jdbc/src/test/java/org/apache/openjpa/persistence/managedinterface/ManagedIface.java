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
package org.apache.openjpa.persistence.managedinterface;

import java.util.*;

import javax.persistence.Entity;
import javax.persistence.Embedded;
import javax.persistence.OneToOne;
import javax.persistence.OneToMany;
import javax.persistence.CascadeType;

import org.apache.openjpa.persistence.PersistentCollection;
import org.apache.openjpa.persistence.ManagedInterface;
import org.apache.openjpa.persistence.query.SimpleEntity;

@ManagedInterface
@Entity
public interface ManagedIface extends ManagedInterfaceSup {
    public int getIntField();
    public void setIntField(int i);

    @Embedded
    public ManagedInterfaceEmbed getEmbed();
    public void setEmbed(ManagedInterfaceEmbed embed);

    @OneToOne(cascade=CascadeType.PERSIST)
    public ManagedIface getSelf();
    public void setSelf(ManagedIface iface);

    @PersistentCollection
    public Set<Integer> getSetInteger();
    public void setSetInteger(Set<Integer> collection);

    @OneToMany(cascade=CascadeType.PERSIST)
    public Set<SimpleEntity> getSetPC();
    public void setSetPC(Set<SimpleEntity> collection);

    @OneToMany(cascade=CascadeType.PERSIST)
    public Set<ManagedIface> getSetI();
    public void setSetI(Set<ManagedIface> collection);

    @OneToOne(cascade=CascadeType.PERSIST)
    public SimpleEntity getPC();
    public void setPC(SimpleEntity pc);

    public void unimplemented();
}
