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

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "seqAssigned")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@IdClass(SequenceAssigned.SeqId.class)
public class SequenceAssigned {

    @Id
    private long pk;

    @OneToOne(cascade = { CascadeType.PERSIST, CascadeType.REMOVE })
    private SequenceAssigned other;

    public SequenceAssigned() {
    }

    public SequenceAssigned(long pk) {
        this.pk = pk;
    }

    public void setPK(long l) {
        pk = l;
    }

    public long getPK() {
        return pk;
    }

    public void setOther(SequenceAssigned other) {
        this.other = other;
    }

    public SequenceAssigned getOther() {
        return other;
    }

    @SuppressWarnings("serial")
    public static class SeqId implements java.io.Serializable {

        public long pk;

        public SeqId() {
        }

        public SeqId(String str) {
            pk = Long.parseLong(str);
        }

        public int hashCode() {
            return (int) (pk % (long) Integer.MAX_VALUE);
        }

        public String toString() {
            return pk + "";
        }

        public boolean equals(Object o) {
            if (o == null || !(o instanceof SeqId))
                return false;
            return pk == ((SeqId) o).pk;
        }
    }
}
