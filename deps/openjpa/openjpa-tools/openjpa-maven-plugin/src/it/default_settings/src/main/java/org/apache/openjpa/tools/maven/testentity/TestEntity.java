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
package org.apache.openjpa.tools.maven.testentity;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Enumerated;
import javax.persistence.EnumType;


@Entity
public class TestEntity {

    @Id
    private int xint1;

    private String string1;

    public enum SampleEnum {
        Option1, Option2, Option3
    }

    @Enumerated(EnumType.STRING)
    private SampleEnum myEnum;


    protected TestEntity() {
    }

    public TestEntity(int int1, String string1) {
        this.xint1 = int1;
        this.string1 = string1;
    }

    public int getInt1() {
        return xint1;
    }

    public void setInt1(int int1) {
        this.xint1 = int1;
    }

    public String getString1() {
        return string1;
    }

    public void setString1(String string1) {
        this.string1 = string1;
    }

    public String toString()  {
        return xint1 + ":" + string1;
    }

    public SampleEnum getMyEnum() {
        return myEnum;
    }

    public void setMyEnum(SampleEnum myEnum) {
        this.myEnum = myEnum;
    }

}
