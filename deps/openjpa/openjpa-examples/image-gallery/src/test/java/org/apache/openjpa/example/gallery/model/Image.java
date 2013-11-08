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
package org.apache.openjpa.example.gallery.model;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.apache.openjpa.example.gallery.ImageType;
import org.apache.openjpa.example.gallery.constraint.ImageConstraint;
import org.apache.openjpa.example.gallery.constraint.ImageGroup;

/**
 * Image entity which makes use of several BV constraints.
 */
@Entity
@ImageConstraint(groups=ImageGroup.class)
public class Image {
    
    private long id;
    private ImageType type;
    private String fileName;
    private byte[] data;    
    private Location location;
    private Creator creator;
    
    @Id
    @GeneratedValue
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @NotNull(message="Image type must be specified.")
    @Enumerated(EnumType.STRING)
    public ImageType getType() {
        return type;
    }
    
    public void setType(ImageType type) {
        this.type = type;
    }
    
    @NotNull(message="Image file name must not be null.")
    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @NotNull(message="Image data must not be null.")
    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    @Valid
    @Embedded
    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    @ManyToOne
    public Creator getCreator() {
        return creator;
    }

    public void setCreator(Creator creator) {
        this.creator = creator;
    }
}
