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

import javax.persistence.Entity;

import java.util.*;
import java.io.*;


@Entity
public class AttachF
    implements Serializable {

    private int id1;
    private String id2;

    private String fstr;
    private int fint;
    private double fdbl;
    private AttachE e;
    private List strings = new ArrayList();

    private AttachC embeddedC;

    public void setFstr(String fstr) {
        this.fstr = fstr;
    }

    public String getFstr() {
        return this.fstr;
    }

    public void setFint(int fint) {
        this.fint = fint;
    }

    public int getFint() {
        return this.fint;
    }

    public void setFdbl(double fdbl) {
        this.fdbl = fdbl;
    }

    public double getFdbl() {
        return this.fdbl;
    }

    public void setE(AttachE e) {
        this.e = e;
    }

    public AttachE getE() {
        return this.e;
    }

    public void setStrings(List strings) {
        this.strings = strings;
    }

    public List getStrings() {
        return this.strings;
    }

    public static class ID {

        public int id1;
        public String id2;

        public ID() {
        }

        public ID(String str) {
            StringTokenizer tok = new StringTokenizer(str, ":");
            id1 = Integer.parseInt(tok.nextToken());
            id2 = tok.nextToken();
        }

        public int hashCode() {
            return id1 + (id2 == null ? 0 : id2.hashCode());
        }

        public String toString() {
            return id1 + ":" + id2;
        }

        public boolean equals(Object other) {
            return other instanceof ID
                && ((ID) other).id1 == id1
                && (id2 == null ? ((ID) other).id2 == null
                : id2.equals(((ID) other).id2));
        }
    }

    public void setId1(int id1) {
        this.id1 = id1;
    }

    public int getId1() {
        return this.id1;
    }

    public void setId2(String id2) {
        this.id2 = id2;
    }

    public String getId2() {
        return this.id2;
    }

    public void setEmbeddedC(AttachC embeddedC) {
        this.embeddedC = embeddedC;
    }

    public AttachC getEmbeddedC() {
        return this.embeddedC;
    }
}
