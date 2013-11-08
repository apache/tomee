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
package org.apache.openjpa.persistence.generationtype;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;

@Entity
public class GeneratedValues {
    @Id
    @GeneratedValue
    private int id;

    @GeneratedValue
    private long field;

//    @GeneratedValue(strategy= GenerationType.SEQUENCE,
//        generator="org.apache.openjpa.persistence.generationtype.CustomSeq")
//    private int customSeqField;

    @GeneratedValue(strategy= GenerationType.SEQUENCE,
        generator="GeneratedValues.SequenceGeneratorCustomSeq")
    @SequenceGenerator(name="GeneratedValues.SequenceGeneratorCustomSeq",
        sequenceName=
            "org.apache.openjpa.persistence.generationtype.CustomSeq()")
    private int customSeqWithIndirectionField;

    @GeneratedValue(generator="uuid-hex")
    private String uuidhex;

    @GeneratedValue(generator="uuid-string")
    private String uuidstring;

    @GeneratedValue(generator="uuid-type4-hex")
    private String uuidT4hex;

    @GeneratedValue(generator="uuid-type4-string")
    private String uuidT4string;
    
    public GeneratedValues() {
        super();
    }
    
    public GeneratedValues(int id, long field, String uh, String us,
        String ut4h, String ut4s) {
        super();
        this.id = id;
        this.field = field;
        this.uuidhex = uh;
        this.uuidstring = us;
        this.uuidT4hex = ut4h;
        this.uuidT4string = ut4s;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getField() {
        return field;
    }

    public void setField(long field) {
        this.field = field;
    }

//    public int getCustomSeqField() {
//        return customSeqField;
//    }

    public int getCustomSeqWithIndirectionField() {
        return customSeqWithIndirectionField;
    }
    
    public void setUuidhex(String uuidhex) {
        this.uuidhex = uuidhex;
    }

    public String getUuidhex() {
        return uuidhex;
    }

    public void setUuidstring(String uuidstring) {
        this.uuidstring = uuidstring;
    }

    public String getUuidstring() {
        return uuidstring;
    }

    public void setUuidT4hex(String uuidT4hex) {
        this.uuidT4hex = uuidT4hex;
    }

    public String getUuidT4hex() {
        return uuidT4hex;
    }

    public void setUuidT4string(String uuidT4string) {
        this.uuidT4string = uuidT4string;
    }

    public String getUuidT4string() {
        return uuidT4string;
    }
}
