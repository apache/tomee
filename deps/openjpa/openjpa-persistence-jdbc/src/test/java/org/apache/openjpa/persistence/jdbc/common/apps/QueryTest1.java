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
package org.apache.openjpa.persistence.jdbc.common.apps;

import java.util.*;

/**
 * <p>Used in testing; should be enhanced.</p>
 *
 * @author Abe White
 */
public class QueryTest1 {

    public static final long FIVE = 5L;

    private long num = 0L;
    private String string = null;
    private String clobField = null;
    private boolean bool = false;
    private float decimal = 0F;
    private char character = ' ';
    private Date date = null;
    private Collection manyToMany = null;

    public long getNum() {
        return num;
    }

    public void setNum(long val) {
        num = val;
    }

    public String getString() {
        return string;
    }

    public void setString(String val) {
        string = val;
    }

    public String getClob() {
        return clobField;
    }

    public void setClob(String val) {
        clobField = val;
    }

    public boolean getBool() {
        return bool;
    }

    public void setBool(boolean val) {
        bool = val;
    }

    public float getDecimal() {
        return decimal;
    }

    public void setDecimal(float val) {
        decimal = val;
    }

    public char getCharacter() {
        return character;
    }

    public void setCharacter(char val) {
        character = val;
    }

    public void setDate(Date val) {
        date = val;
    }

    public Date getDate() {
        return date;
    }

    public Collection getManyToMany() {
        return manyToMany;
    }

    public void setManyToMany(Collection val) {
        manyToMany = val;
    }
}
