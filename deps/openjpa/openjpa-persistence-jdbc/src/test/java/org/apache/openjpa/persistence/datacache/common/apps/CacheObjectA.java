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
package org.apache.openjpa.persistence.datacache.common.apps;

import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.Locale;
import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.apache.openjpa.persistence.PersistentCollection;
import org.apache.openjpa.persistence.jdbc.ContainerTable;
import org.apache.openjpa.persistence.jdbc.ElementJoinColumn;
import org.apache.openjpa.persistence.jdbc.OrderColumn;
import org.apache.openjpa.persistence.jdbc.XJoinColumn;

/**
 * Used in testing; should be enhanced.
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "TYP")
@DiscriminatorValue("CACHE_A")
public class CacheObjectA {

    private String name = null;

    private long age = 0;

    @ManyToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    private CacheObjectA relatedObj = null;

    @PersistentCollection
    @ContainerTable(name = "CACHE_STRINGCOLL",
        joinColumns = @XJoinColumn(name = "ID"))
    @ElementJoinColumn(name = "ELEMENT")
    /*
      * @ManyToMany @JoinTable(name="CACHE_STRINGCOLL",
      * joinColumns=@JoinColumn(name="ID"),
      * inverseJoinColumns=@JoinColumn(name="ELEMENT"))
      */
    private Collection<String> stringColl = new LinkedList();

    /*
      * @ManyToMany @JoinTable(name="CACHE_RELATEDCOLL",
      * joinColumns=@JoinColumn(name="ID"),
      * inverseJoinColumns=@JoinColumn(name="ELEMENT"))
      */
    @PersistentCollection
    @ContainerTable(name = "CACHE_RELATEDCOLL",
        joinColumns = @XJoinColumn(name = "ID"))
    @ElementJoinColumn(name = "ELEMENT")
    private Collection<CacheObjectA> relatedColl = new LinkedList();

    /*
      * @PersistentCollection @ContainerTable(name="CACHE_AS",
      * joinColumns=@XJoinColumn(name="ID")) @ElementJoinColumn(name="ELEMENT")
      * @OrderColumn(name="ORDR")
      */
    @ManyToMany
    @JoinTable(name = "CACHE_AS", joinColumns = @JoinColumn(name = "ID"),
        inverseJoinColumns = @JoinColumn(name = "ELEMENT"))
    private CacheObjectA[] as;

    @PersistentCollection
    @ContainerTable(name = "CACHE_STRINGARRAY",
        joinColumns = @XJoinColumn(name = "ID"))
    @ElementJoinColumn(name = "ELEMENT")
    @OrderColumn(name = "ORDR")
    /*
      * @ManyToMany @JoinTable(name="CACHE_STRINGARRAY",
      * joinColumns=@JoinColumn(name="ID"),
      * inverseJoinColumns=@JoinColumn(name="ELEMENT"))
      */
    private String[] stringArray;

    @PersistentCollection
    @ContainerTable(name = "CACHE_PRIMITIVEARRAY",
        joinColumns = @XJoinColumn(name = "ID"))
    @ElementJoinColumn(name = "ELEMENT")
    @OrderColumn(name = "ORDR")
    /*
      * @ManyToMany @JoinTable(name="CACHE_PRIMITIVEARRAY",
      * joinColumns=@JoinColumn(name="ID"),
      * inverseJoinColumns=@JoinColumn(name="ELEMENT"))
      */
    private float[] primitiveArray;

    @PersistentCollection
    @ContainerTable(name = "CACHE_DATEARRAY",
        joinColumns = @XJoinColumn(name = "ID"))
    @ElementJoinColumn(name = "ELEMENT")
    @OrderColumn(name = "ORDR")
    /*
      * @ManyToMany @JoinTable(name="CACHE_DATEARRAY",
      * joinColumns=@JoinColumn(name="ID"),
      * inverseJoinColumns=@JoinColumn(name="ELEMENT"))
      */
    private Date[] dateArray;

    @Temporal(TemporalType.DATE)
    private Date date;

    private Locale locale;

    @ManyToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    private CacheObjectB relatedB = null;

    @OneToOne(fetch = FetchType.LAZY, cascade = { CascadeType.ALL })
    private CacheObjectInterface relatedInterface = null;

    public CacheObjectA() {
    }

    public CacheObjectA(String name, long age) {
        this.name = name;
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getAge() {
        return age;
    }

    public void setAge(long age) {
        this.age = age;
    }

    public CacheObjectA getRelatedObject() {
        return relatedObj;
    }

    public void setRelatedObject(CacheObjectA o) {
        relatedObj = o;
    }

    public Collection getStringCollection() {
        return stringColl;
    }

    public void setStringCollection(Collection coll) {
        stringColl = coll;
    }

    public Collection getRelatedCollection() {
        return relatedColl;
    }

    public void setRelatedCollection(Collection coll) {
        relatedColl = coll;
    }

    public CacheObjectA[] getRelatedArray() {
        return as;
    }

    public void setRelatedArray(CacheObjectA[] array) {
        as = array;
    }

    public String[] getStringArray() {
        return stringArray;
    }

    public void setStringArray(String[] array) {
        stringArray = array;
    }

    public void setPrimitiveArray(float[] val) {
        primitiveArray = val;
    }

    public float[] getPrimitiveArray() {
        return primitiveArray;
    }

    public void setDateArray(Date[] val) {
        dateArray = val;
    }

    public Date[] getDateArray() {
        return dateArray;
    }

    public void setDate(Date val) {
        date = val;
    }

    public Date getDate() {
        return date;
    }

    public void setLocale(Locale val) {
        locale = val;
    }

    public Locale getLocale() {
        return locale;
    }

    public void setRelatedB(CacheObjectB val) {
        relatedB = val;
    }

    public CacheObjectB getRelatedB() {
        return relatedB;
    }

    public void setRelatedInterface(CacheObjectInterface val) {
        relatedInterface = val;
    }

    public CacheObjectInterface getRelatedInterface() {
        return relatedInterface;
    }

    public String toString() {
        try {
            return "CacheObjectA: " + super.toString() + "; name: " + name
                + "; age: " + age;
        }
        catch (Exception e) {
            return "CacheObjectA: " + super.toString() + "; "
                + "Exception in toString(): " + e;
        }
    }
}
