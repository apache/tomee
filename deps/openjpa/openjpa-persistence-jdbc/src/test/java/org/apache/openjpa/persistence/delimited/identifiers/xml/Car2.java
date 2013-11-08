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
package org.apache.openjpa.persistence.delimited.identifiers.xml;

//@SqlResultSetMapping(name="CarResultSet",
//    entities={@EntityResult(entityClass=Car2.class,
//        fields={
//        @FieldResult(name="model", column="car model"),
//        @FieldResult(name="color", column="car color")
//    },
//    discriminatorColumn="discr col")},
//    columns={@ColumnResult(name="model year")})
//    
//@Entity
//@Inheritance
//@DiscriminatorColumn(name="discr col", columnDefinition="VARCHAR(10)")
//@Table(name="Car2")
public class Car2 {
//    @Id
    private int id;
    
    protected String model;
    protected String color;

    public Car2() {}
    
    public Car2(int id) {
        this.id = id;
    }
    
    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * @return the model
     */
    public String getModel() {
        return model;
    }

    /**
     * @param type the type to set
     */
    public void setModel(String model) {
        this.model = model;
    }

    /**
     * @return the color
     */
    public String getColor() {
        return color;
    }

    /**
     * @param name the name to set
     */
    public void setName(String color) {
        this.color = color;
    }

}
