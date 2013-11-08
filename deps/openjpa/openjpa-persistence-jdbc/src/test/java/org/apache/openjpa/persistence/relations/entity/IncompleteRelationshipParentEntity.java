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
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;

/**
 * This is the parent entity for the IncompleteRelationship test case.
 */
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public class IncompleteRelationshipParentEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Embeddable
    public static class IncompleteRelationshipParentEntityPk implements Serializable {
        private static final long serialVersionUID = 1L;

        @Column(name = "ID", nullable = false, precision = 9)
        protected int id;
        @Column(name = "CLIENT_ID", nullable = false, length = 35)
        protected String clientId;

        public IncompleteRelationshipParentEntityPk() {
        }

        public IncompleteRelationshipParentEntityPk(int id, String clientId) {
            this.id = id;
            this.clientId = clientId;
        }

        public String getClientId() {
            return clientId;
        }

        public void setClientId(String clientId) {
            this.clientId = clientId;
        }

        public void setId(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final IncompleteRelationshipParentEntityPk other = (IncompleteRelationshipParentEntityPk) obj;
            if (this.id != other.id) {
                return false;
            }
            if ((this.clientId == null) ? (other.clientId != null) : !this.clientId.equals(other.clientId)) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 89 * hash + (this.id);
            hash = 89 * hash + (this.clientId != null ? this.clientId.hashCode() : 0);
            return hash;
        }

        @Override
        public String toString() {
            return "IncompleteRelationshipParentEntityPk[id="+id+","+
                    "clientId="+clientId+"]";
        }
    }

    @EmbeddedId
    protected IncompleteRelationshipParentEntityPk pk;
    @JoinColumns({
        @JoinColumn(name = "DEFAULT_DISCOUNT", referencedColumnName = "DISCOUNT"),
        @JoinColumn(name = "CLIENT_ID", referencedColumnName = "CLIENT_ID")
    })
    @ManyToOne(fetch = FetchType.LAZY)
    protected IncompleteRelationshipChildEntity child;

    public IncompleteRelationshipParentEntity() {
    }

    public IncompleteRelationshipParentEntity(IncompleteRelationshipParentEntityPk pk) {
        this.pk = pk;
    }

    public IncompleteRelationshipParentEntity(int id, String clientId) {
        this(new IncompleteRelationshipParentEntityPk(id, clientId));
    }

    public IncompleteRelationshipChildEntity getChild() {
        return child;
    }

    public void setChild(IncompleteRelationshipChildEntity child) {
        this.child = child;
    }

    public IncompleteRelationshipParentEntityPk getPk() {
        return pk;
    }

    public void setPk(IncompleteRelationshipParentEntityPk pk) {
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
        final IncompleteRelationshipParentEntity other = (IncompleteRelationshipParentEntity) obj;
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
            return "IncompleteRelationshipParentEntity[pk=null]";
        }
    }
}
