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
package org.apache.openjpa.persistence.lock.extended;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinTable;
import javax.persistence.LockModeType;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.QueryHint;
import javax.persistence.Version;

@NamedQueries ( value={
        @NamedQuery(
            name="findLSE1x1LfJTLzyNormal"
            , query="SELECT c FROM LSE1x1LfJTLzy c WHERE c.firstName LIKE :firstName"
            , lockMode=LockModeType.PESSIMISTIC_WRITE
            ),
        @NamedQuery(
            name="findLSE1x1LfJTLzyExtended"
            , query="SELECT c FROM LSE1x1LfJTLzy c WHERE c.firstName LIKE :firstName"
            , lockMode=LockModeType.PESSIMISTIC_WRITE
            , hints={@QueryHint(name="javax.persistence.lock.scope",value="EXTENDED")}
            ) 
        }
    )

@Entity
public class LSE1x1LfJTLzy implements Externalizable {

    @Id
    private int id;

    @Version
    private int version;

    private String firstName;
    
    @OneToOne(fetch=FetchType.LAZY)
    @JoinTable(name="Uni1x1LfJT_Uni1x1RT")
    private LSE1x1Rt uniRightJT;

    public int getId() {
        return id;
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
    
    public LSE1x1Rt getUniRightJT() {
        return uniRightJT;
    }

    public void setUniRightJT(LSE1x1Rt uniRightJT) {
        this.uniRightJT = uniRightJT;
    }

    public int getVersion() {
        return version;
    }

    public String toString() {
        return this.getClass().getName() + '@' + Integer.toHexString(System.identityHashCode(this)) + "[id=" + getId()
                + ", ver=" + getVersion() + ", firstName=" + firstName + "]" 
                + " uniRightJT=" + getUniRightJT()
                ;
    }

    public void readExternal(ObjectInput in) throws IOException,
        ClassNotFoundException {
        id = in.readInt();
        version = in.readInt();
        firstName = in.readUTF();
        uniRightJT = (LSE1x1Rt) in.readObject();
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(id);
        out.writeInt(version);
        out.writeUTF(firstName);
        out.writeObject(uniRightJT);
    }
}
