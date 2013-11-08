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
package
    org.apache.openjpa.persistence.annotations.common.apps.annotApp.annotype;

import java.io.Serializable;

public class PartyId implements Serializable{

    private static final long serialVersionUID = 1L;
    private String partyName;
    private int id;

    public PartyId() {}

    public  PartyId(String partyName, int id) {
        this.partyName = partyName;
        this.id = id;
    }

    public String getPartyName() {
        return this.partyName;
    }

    public int getId() {
        return this.id;
    }

    public boolean equals(Object o) {
        return (o instanceof PartyId) &&
            partyName.equals(((PartyId)o).getPartyName()) &&
            id == (((PartyId)o).getId());
    }

    public int hashCode() {
        return partyName.hashCode() + id;
    }
}
