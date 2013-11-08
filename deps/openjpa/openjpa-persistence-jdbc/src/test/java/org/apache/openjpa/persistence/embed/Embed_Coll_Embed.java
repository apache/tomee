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
package org.apache.openjpa.persistence.embed;

import java.util.List;
import java.util.ArrayList;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embeddable;

import javax.persistence.ElementCollection;

@Embeddable 
public class Embed_Coll_Embed {
    protected int intVal1;
    protected int intVal2;
    protected int intVal3;
    
    @ElementCollection // use default table name
    //@CollectionTable(name="TableA")
    @AttributeOverrides({
        @AttributeOverride(name="intVal1", column=@Column(name="A1")),
        @AttributeOverride(name="intVal2", column=@Column(name="A2")),
        @AttributeOverride(name="intVal3", column=@Column(name="A3"))
    })
    protected List<Embed> embeds = new ArrayList<Embed>();
    
    public int getIntVal1() {
        return intVal1;
    }
    
    public void setIntVal1(int intVal1) {
        this.intVal1 = intVal1;
    }
    
    public int getIntVal2() {
        return intVal2;
    }
    
    public void setIntVal2(int intVal2) {
        this.intVal2 = intVal2;
    }
    
    public int getIntVal3() {
        return intVal3;
    }
    
    public void setIntVal3(int intVal3) {
        this.intVal3 = intVal3;
    }
        
    public List<Embed> getEmbeds() {
        return embeds;
    }
 
    public void addEmbed(Embed embed) {
        embeds.add(embed);
    }
}
