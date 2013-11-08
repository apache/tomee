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
package org.apache.openjpa.persistence.jdbc.query.cache;

import javax.persistence.*;

import org.apache.openjpa.persistence.DataCache;

@Entity
@DataCache(timeout=100000)
@Table(name="Usage1")
public class Usage {
    @Id  
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    int id;
    int quantity;

    @ManyToOne
    Part child;
    @ManyToOne
    PartComposite parent ;

    @Version
    long version;


    public Usage(PartComposite p, int quantity, Part subpart) {
        parent=p;
        this.quantity=quantity;
        parent.getPartsUsed().add(this);
        setChild(subpart);
        subpart.getUsedIn().add(this);
    }

    // JPA entity needs a public no-arg constructor ! 
    public Usage() {}


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Part getParent() {
        return parent;
    }

    public void setParent(PartComposite parent) {
        this.parent = parent;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public Part getChild() {
        return child;
    }

    public void setChild(Part child) {
        this.child = child;
    }

    public String toString() {
        return "Usage:"+id+" quantity:"+quantity+" child:"+child.getPartno()+
            " parent"+parent.getPartno();
    }
}
