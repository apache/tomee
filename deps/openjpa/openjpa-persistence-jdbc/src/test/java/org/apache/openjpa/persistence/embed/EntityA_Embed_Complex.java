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

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import javax.persistence.ElementCollection;
import javax.persistence.CollectionTable;
import javax.persistence.Table;

@Entity
@Table(name="TBL3C")
public class EntityA_Embed_Complex implements Serializable {
    @Id
    Integer id;

    @Column(length=30)
    String name;
    
    @Basic(fetch=FetchType.LAZY)
    int age;

    @ElementCollection(fetch=FetchType.EAGER) 
    @CollectionTable(name="NickNames_Tbl")
    @Column(name="nicknames1", length=20)
    protected Set<String> nickNames = new HashSet<String>();

    @ElementCollection
    @Enumerated(EnumType.ORDINAL)
    protected List<CreditRating> cr = new ArrayList<CreditRating>();
    
    @ElementCollection
    @Temporal(TemporalType.DATE)
    protected List<Timestamp> ts = new ArrayList<Timestamp>();

    @ElementCollection
    @Lob
    protected List<String> lobs = new ArrayList<String>();

    protected Embed_Embed embed;
    
    @ElementCollection
    protected List<Embed_Embed> embeds = new ArrayList<Embed_Embed>();

    @ElementCollection(fetch=FetchType.EAGER)
    @CollectionTable(name="EMBED1ToOneS2") // use default join column name
    @AttributeOverrides({
        @AttributeOverride(name="name1", column=@Column(name="EMB_NAME1")),
        @AttributeOverride(name="name2", column=@Column(name="EMB_NAME2")),
        @AttributeOverride(name="name3", column=@Column(name="EMB_NAME3"))
    })
    protected Set<Embed_ToOne> embed1s = new HashSet<Embed_ToOne>();

    private transient Integer transientJavaValue;

    @Transient
    private Integer transientValue;

    
    /*
     * Getters/Setters
     */
    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<String> getNickNames() {
        return nickNames;
    }
    
    public void addNickName(String nickName) {
        nickNames.add(nickName);
    }

    public List<CreditRating> getCreditRating() {
        return cr;
    }
    
    public void addCreditRating(CreditRating c) {
        cr.add(c);
    }
    
    public List<Timestamp> getTimestamps() {
        return ts;
    }
    
    public void addTimestamp(Timestamp t) {
        ts.add(t);
    }
    
    public List<String> getLobs() {
        return lobs;
    }
    
    public void addLob(String lob) {
        lobs.add(lob);
    }

    public enum CreditRating { POOR, GOOD, EXCELLENT };
    
    public Embed_Embed getEmbed() {
        return embed;
    }
    
    public void setEmbed(Embed_Embed embed) {
        this.embed = embed;
    }

    public List<Embed_Embed> getEmbeds() {
        return embeds;
    }
    
    public void addEmbed(Embed_Embed embed) {
        embeds.add(embed);
    }

    public Set<Embed_ToOne> getEmbed1ToOnes() {
        return embed1s;
    }
    
    public void addEmbed1ToOnes(Embed_ToOne embed1) {
        embed1s.add(embed1);
    }

    public Integer getTransientJavaValue() {
        return this.transientJavaValue;
    }

    public void setTransientJavaValue(Integer transientJavaValue) {
        this.transientJavaValue = transientJavaValue;
    }

    public Integer getTransientValue() {
        return this.transientValue;
    }

    public void setTransientValue(Integer transientValue) {
        this.transientValue = transientValue;
    }
}
