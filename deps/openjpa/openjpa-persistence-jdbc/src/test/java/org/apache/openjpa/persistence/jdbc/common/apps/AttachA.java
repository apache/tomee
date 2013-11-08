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

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name="JDBC_AttachA")
public class AttachA
    implements Serializable, PreDetachCallback,
    PostDetachCallback, PreAttachCallback, PostAttachCallback {

    // transient method for testing callbacks
    public transient Object detachSource = null;
    public transient Object attachSource = null;
    public transient int preAttachCalls = 0;
    public transient int postAttachCalls = 0;
    public transient int preDetachCalls = 0;
    public transient int postDetachCalls = 0;

    private String astr;
    private int aint;
    private double adbl;
    private String [] stringArray = new String [0];
    private AttachE [] attachEArray = new AttachE [0];

    public void jdoPreDetach() {
        preDetachCalls++;
    }

    public void jdoPostDetach(Object orig) {
        postDetachCalls++;
        detachSource = orig;
    }

    public void jdoPreAttach() {
        preAttachCalls++;
    }

    public void jdoPostAttach(Object orig) {
        postAttachCalls++;
        attachSource = orig;
    }

    public void setAstr(String astr) {
        this.astr = astr;
    }

    public String getAstr() {
        return this.astr;
    }

    public void setAint(int aint) {
        this.aint = aint;
    }

    public int getAint() {
        return this.aint;
    }

    public void setAdbl(double adbl) {
        this.adbl = adbl;
    }

    public double getAdbl() {
        return this.adbl;
    }

    public void setStringArray(String [] stringArray) {
        this.stringArray = stringArray;
    }

    public String [] getStringArray() {
        return this.stringArray;
    }

    public void setAttachEArray(AttachE [] attachEArray) {
        this.attachEArray = attachEArray;
    }

    public AttachE [] getAttachEArray() {
        return this.attachEArray;
    }

    private void writeObject(ObjectOutputStream out)
        throws IOException {
        out.defaultWriteObject();
    }

    private void readObject(ObjectInputStream in)
        throws IOException, ClassNotFoundException {
        in.defaultReadObject();
    }
}
