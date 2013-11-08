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

import java.io.*;
import java.util.*;

import javax.persistence.Entity;

@Entity
public class HorizAppSingleA
    implements HorizAppA {

    // initialize PK values to random values
	//FIXME 
	/*
    private String pk1 = "PK-" + Strings.getClassName(getClass().getName());
    private int pk2 = com.solarmetric.test.AbstractTestCase.
        randomInt().intValue();
	*/
	private String pk1;
    private int pk2;

    private String stringA;
    private int intA;
    private List relations = new ArrayList();

    public void setPk1(String pk1) {
        this.pk1 = pk1;
    }

    public String getPk1() {
        return this.pk1;
    }

    public void setPk2(int pk2) {
        this.pk2 = pk2;
    }

    public int getPk2() {
        return this.pk2;
    }

    public void setStringA(String stringA) {
        this.stringA = stringA;
    }

    public String getStringA() {
        return this.stringA;
    }

    public void setIntA(int intA) {
        this.intA = intA;
    }

    public int getIntA() {
        return this.intA;
    }

    public static class ID
        implements Serializable {

        public String pk1;
        public int pk2;

        public ID() {
        }

        public ID(String str) {
            StringTokenizer tok = new StringTokenizer(str, ":");
            pk1 = tok.nextToken();
            pk2 = Integer.parseInt(tok.nextToken());
        }

        public String toString() {
            return pk1 + ":" + pk2;
        }

        public int hashCode() {
            return (pk2 + (pk1 == null ? 0 : pk1.hashCode()))
                % Integer.MAX_VALUE;
        }

        public boolean equals(Object other) {
            return other instanceof ID
                && ((ID) other).pk2 == pk2
                && (((ID) other).pk1 == null
                ? pk1 == null
                : ((ID) other).pk1.equals(pk1));
        }
    }

    public void setRelations(List relations) {
        this.relations = relations;
    }

    public List getRelations() {
        return this.relations;
    }
}



