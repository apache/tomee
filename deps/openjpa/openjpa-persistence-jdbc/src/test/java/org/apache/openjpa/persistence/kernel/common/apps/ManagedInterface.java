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

import java.util.Set;

public interface ManagedInterface extends ManagedInterfaceSup {

    public int getIntField();

    public void setIntField(int i);

    public ManagedInterfaceEmbed getEmbed();

    public void setEmbed(ManagedInterfaceEmbed embed);

    public ManagedInterface getSelf();

    public void setSelf(ManagedInterface iface);

    public Set getSetInteger();

    public void setSetInteger(Set collection);

    public Set getSetPC();

    public void setSetPC(Set collection);

    public Set getSetI();

    public void setSetI(Set collection);

    public RuntimeTest1 getPC();

    public void setPC(RuntimeTest1 pc);

    public void unimplemented();
}
