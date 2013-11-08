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
package org.apache.openjpa.persistence.relations;

import java.util.Collection;
import java.util.HashSet;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Version;

@Entity
public class Util1xmLf {

    private int id;

    private int version;

    private String firstName;

    public Collection<Util1xmRt> uniRightLzy = new HashSet<Util1xmRt>();

    public Collection<Util1xmRt> uniRightEgr = new HashSet<Util1xmRt>();

    @Id
    public int getId() {
        return id;
    }

    @Version
    public int getVersion() {
        return version;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    @OneToMany// (fetch = FetchType.LAZY)
    public Collection<Util1xmRt> getUniRightLzy() {
        return uniRightLzy;
    }

    public void setUniRightLzy(Collection<Util1xmRt> uniRightLzy) {
        this.uniRightLzy = uniRightLzy;
    }

    public void addUniRightLzy(Util1xmRt uniRightLzy) {
        getUniRightLzy().add(uniRightLzy);
    }

    @OneToMany(fetch = FetchType.EAGER)
    public Collection<Util1xmRt> getUniRightEgr() {
        return uniRightEgr;
    }

    public void setUniRightEgr(Collection<Util1xmRt> uniRightEgr) {
        this.uniRightEgr = uniRightEgr;
    }

    public void addUniRightEgr(Util1xmRt uniRightEgr) {
        getUniRightEgr().add(uniRightEgr);
    }
}
