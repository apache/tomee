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

package org.apache.openjpa.persistence.relations.entity;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;

/**
 * This is the child entity for the IncompleteRelationship test case.
 */
@Entity
public class IncompleteRelationshipChildEntity implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @Embeddable
    public static class IncompleteRelationshipChildEntityPk implements Serializable {
        private static final long serialVersionUID = 1L;
        
        @Column(name = "DISCOUNT", nullable = false, length = 120)
        protected String discount;
        @Column(name = "CLIENT_ID", nullable = false, length = 35)
        protected String clientId;

        public IncompleteRelationshipChildEntityPk() {
        }

        public IncompleteRelationshipChildEntityPk(String discount, String clientId) {
            this.discount = discount;
            this.clientId = clientId;
        }

        public String getClientId() {
            return clientId;
        }

        public void setClientId(String clientId) {
            this.clientId = clientId;
        }

        public String getDiscount() {
            return discount;
        }

        public void setDiscount(String discount) {
            this.discount = discount;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final IncompleteRelationshipChildEntityPk other = (IncompleteRelationshipChildEntityPk) obj;
            if ((this.discount == null) ? (other.discount != null) : !this.discount.equals(other.discount)) {
                return false;
            }
            if ((this.clientId == null) ? (other.clientId != null) : !this.clientId.equals(other.clientId)) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 53 * hash + (this.discount != null ? this.discount.hashCode() : 0);
            hash = 53 * hash + (this.clientId != null ? this.clientId.hashCode() : 0);
            return hash;
        }

        @Override
        public String toString() {
            return "IncompleteRelationshipChildEntityPk[discount="+discount+","+
                    "clientId="+clientId+"]";
        }
    }

    @EmbeddedId
    protected IncompleteRelationshipChildEntityPk pk;

    public IncompleteRelationshipChildEntity() {
    }

    public IncompleteRelationshipChildEntity(IncompleteRelationshipChildEntityPk pk) {
        this.pk = pk;
    }

    public IncompleteRelationshipChildEntity(String discount, String clientId) {
        this(new IncompleteRelationshipChildEntityPk(discount, clientId));
    }

    public IncompleteRelationshipChildEntityPk getPk() {
        return pk;
    }

    public void setPk(IncompleteRelationshipChildEntityPk pk) {
        this.pk = pk;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final IncompleteRelationshipChildEntity other = (IncompleteRelationshipChildEntity) obj;
        if (this.pk != other.pk && (this.pk == null || !this.pk.equals(other.pk))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        if (this.pk != null) {
            return pk.hashCode();
        } else {
            return 0;
        }
    }

    @Override
    public String toString() {
        if (this.pk != null) {
            return pk.toString();
        } else {
            return "IncompleteRelationshipChildEntity[pk=null]";
        }
    }
}
