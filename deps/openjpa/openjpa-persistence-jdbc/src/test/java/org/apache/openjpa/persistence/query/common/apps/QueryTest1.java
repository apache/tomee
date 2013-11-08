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
package org.apache.openjpa.persistence.query.common.apps;

import java.util.Date;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

/**
 * <p>Used in testing; should be enhanced.</p>
 *
 * @author Abe White
 */
@Entity
@NamedQueries({
@NamedQuery(name = "named",
    query = "SELECT o FROM QueryTest1 o"),
@NamedQuery(name = "sql",
    query = "select * from foo"),
@NamedQuery(name = "systemsql",
    query = "select * from foo"),
@NamedQuery(name = "systemjdoql",
    query = "select o FROM QueryTest1 where o.numb == 4")
    })
public class QueryTest1 implements EntityInterface {

    /*
      * Changed Variable names : Afam Okeke
      * Reason: The old var names are reserved my some DB's namely MYSQL.
      */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public int id;

    public static final long FIVE = 5L;

    private long numb = 0L;
    private String strong = null;

    @Column(length = -1)
    private String cField = null;
    private boolean boolt = false;
    private float decar = 1.0f;
    private char chart = ' ';
    private Date datum = null;

    @ManyToMany(mappedBy = "manyToMany3")
    private List<QueryTest4> manyToMany = null;

    public QueryTest1() {
        decar = 1.0f;
    }

    public QueryTest1(int id) {
        decar = 1.0f;
        this.id = id;
    }

    public long getNum() {
        return numb;
    }

    public void setNum(long val) {
        numb = val;
    }

    public String getString() {
        return strong;
    }

    public void setString(String val) {
        strong = val;
    }

    public String getClob() {
        return cField;
    }

    public void setClob(String val) {
        cField = val;
    }

    public boolean getBool() {
        return boolt;
    }

    public void setBool(boolean val) {
        boolt = val;
    }

    public float getDecimal() {
        return decar;
    }

    public void setDecimal(float val) {
        decar = val;
    }

    public char getCharacter() {
        return chart;
    }

    public void setCharacter(char val) {
        chart = val;
    }

    public void setDate(Date val) {
        datum = val;
    }

    public Date getDate() {
        return datum;
    }

    public List<QueryTest4> getManyToMany() {
        return manyToMany;
    }

    public void setManyToMany(List<QueryTest4> val) {
        manyToMany = val;
    }

    public int getId() {
        return this.id;
    }
}
