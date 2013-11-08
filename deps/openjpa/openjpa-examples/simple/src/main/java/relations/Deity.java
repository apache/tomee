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
package relations;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.EnumType;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.NamedQueries;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;


/** 
 * An entity that contains relations corresponding to family tree relations.
 * This entity demonstrates the following JPA features:
 *
 * 1. Enum fields (gender)
 * 2. @OneToOne relations
 * 3. @OneToMany relations
 * 4. Named queries
 */
@Entity
@NamedQueries({

    // a sibling shares a mother and a father
    @NamedQuery(name="siblings", query="select distinct sibling1 "
        + "from Deity sibling1, Deity sibling2 where "
        + "sibling1.father = sibling2.father "
        + "and sibling1.mother = sibling2.mother "
        + "and sibling2 = ?1 and sibling1 <> ?1"),

    // a half-siling shares a mother or a father, but not both
    @NamedQuery(name="half-siblings", query="select distinct sibling1 "
        + "from Deity sibling1, Deity sibling2 where "
        + "((sibling1.father = sibling2.father "
        + "and sibling1.mother <> sibling2.mother) "
        + "or (sibling1.father <> sibling2.father "
        + "and sibling1.mother = sibling2.mother)) "
        + "and sibling2 = ?1 and sibling1 <> ?1"),

    // a cousin shares a grandparent, but is not a sibling
    @NamedQuery(name="cousins", query="select distinct cousin1 "
        + "from Deity cousin1, Deity cousin2 where "
        + "("
            + "cousin1.father.father = cousin2.father.father "
            + "or cousin1.father.mother = cousin2.father.mother "
            + "or cousin1.mother.father = cousin2.mother.father "
            + "or cousin1.mother.mother = cousin2.mother.mother) "
        + "and (cousin1.father <> cousin2.father) "
        + "and (cousin1.mother <> cousin2.mother) "
        + "and cousin2 = ?1 and cousin1 <> ?1")
    })
public class Deity implements Serializable {
    // the Id is the name, which is generally a bad idea, but we are
    // confident that diety names will be unique
    @Id
    private String name;

    @Basic @Enumerated(EnumType.STRING)
    private Gender gender;

    @OneToOne(cascade=CascadeType.ALL)
    private Deity mother;

    @OneToOne(cascade=CascadeType.ALL)
    private Deity father;

    @OneToMany(cascade=CascadeType.ALL)
    private Set<Deity> children;

    public static enum Gender { MALE, FEMALE }


    public Deity(String name, Gender gender) {
        this.name = name;
        this.gender = gender;
    }


    //////////////////////////
    // Business methods follow
    //////////////////////////

    /** 
     * She's having a baby... 
     *  
     * @param  childName  the baby name
     * @return the new child
     *
     * @throws IllegalArgumentException if the person is not a woman, or
     *                                  if the person is unmarried (illegitimate
     *                                  children are not yet supported)
     */
    public Deity giveBirth(String childName, Deity childFather, Gender gender) {
        if (this.gender != Gender.FEMALE)
            throw new IllegalArgumentException("Only women can have children!");

        if (childName == null)
            throw new IllegalArgumentException("No child name!");

        // create the child
        Deity child = new Deity(childName, gender);

        // set the parents in the children...
        child.mother = this;

        // add the child to this member's children
        if (children == null)
            children = new HashSet<Deity>();
        children.add(child);

        if (childFather != null) {
            child.father = childFather;
            if (childFather.children == null)
                childFather.children = new HashSet<Deity>();
            childFather.children.add(child);
        }

        return child;
    }


    ////////////////////////////////////
    // Property accessor methods follow
    ////////////////////////////////////


    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }


    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public Gender getGender() {
        return this.gender;
    }


    public void setMother(Deity mother) {
        this.mother = mother;
    }

    public Deity getMother() {
        return this.mother;
    }


    public void setFather(Deity father) {
        this.father = father;
    }

    public Deity getFather() {
        return this.father;
    }


    public void setChildren(Set<Deity> children) {
        this.children = children;
    }

    public Set<Deity> getChildren() {
        return this.children;
    }

}

