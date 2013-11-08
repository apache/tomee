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
package org.apache.openjpa.jdbc.persistence.classcriteria;

import javax.persistence.*;

@Entity
@DiscriminatorValue(value = "BOOK")
public class Book extends Item {
    private static final long serialVersionUID = 7150584274453979159L;

    @Column(name = "PAGE_COUNT")
    private int pageCount;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @Column(name = "ARTIST")
    private Artist artist1;

    public Book() {
        super();
    }

    public Book(String title) {
        super(title);
    }

    public int getPageCount() {
        return pageCount;
    }

    public void setPageCount(int d) {
        if (d < 0) {
            throw new IllegalArgumentException("Invalid page count " + d + " for " + this);
        }

        pageCount = d;
    }

    public Artist getArtist() {
        return artist1;
    }

    public void setArtist(Artist artist1) {
        this.artist1 = artist1;
    }
}
