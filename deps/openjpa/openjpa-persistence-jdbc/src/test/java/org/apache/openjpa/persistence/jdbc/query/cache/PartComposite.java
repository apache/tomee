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

import java.util.ArrayList;
import java.util.Collection;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.OneToMany;

import org.apache.openjpa.persistence.DataCache;


@Entity
@DataCache
public class PartComposite extends  Part  {

    double assemblyCost;
    double assemblyTime;
    double massIncrement;

    @OneToMany( mappedBy="parent")
    Collection<Usage> partsUsed = new ArrayList<Usage>();

    public PartComposite() {}

    public PartComposite(int partno, String name, double asmCost,
            double massInc) {
        this.partno=partno;
        this.name=name;
        assemblyCost=asmCost;
        massIncrement=massInc;
        inventory=0;
    }

    public PartComposite addSubPart(EntityManager em, int quantity,
            Part subpart) {
        Usage use = new Usage( this, quantity, subpart);
        em.persist(use);
        return this;
    }

    public double getAssemblyCost() {
        return assemblyCost;
    }

    public void setAssemblyCost(double assemblyCost) {
        this.assemblyCost = assemblyCost;
    }


    public double getMassIncrement() {
        return massIncrement;
    }

    public void setMassIncrement(double massIncrement) {
        this.massIncrement = massIncrement;
    }

    public String toString() {

        return "PartComposite:"+partno+" name:+"+name+" assemblyCost:"+
            assemblyCost+" massIncrement:"+massIncrement;
    }

    public Collection<Usage> getPartsUsed() {
        return partsUsed;
    }

    public void setPartsUsed(Collection<Usage> partsUsed) {
        this.partsUsed = partsUsed;
    }

    public double getAssemblyTime() {
        return assemblyTime;
    }

    public void setAssemblyTime(double assemblyTime) {
        this.assemblyTime = assemblyTime;
    }
}
