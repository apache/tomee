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
package org.apache.openjpa.persistence.merge;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@IdClass(GrandChildPK.class)
@Table(name = "MRG_GRANDCHILD")
public class GrandChild implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @JoinColumns({ @JoinColumn(name = "KEY_1", referencedColumnName = "KEY_1"),
        @JoinColumn(name = "KEY_2", referencedColumnName = "KEY_2"),
        @JoinColumn(name = "KEY_3", referencedColumnName = "KEY_3") })
    @ManyToOne
    private Child child;

    @Id
    @Column(name = "KEY_4")
    private Integer grandChildKey;

    public Child getChild() {
        return child;
    }

    public void setChild(Child child) {
        this.child = child;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((child == null) ? 0 : child.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        GrandChild other = (GrandChild) obj;
        if (child == null) {
            if (other.child != null)
                return false;
        } else if (!child.equals(other.child))
            return false;
        return true;
    }

    public Integer getGrandChildKey() {
        return grandChildKey;
    }

    public void setGrandChildKey(Integer grandChildKey) {
        this.grandChildKey = grandChildKey;

    }
}
