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
package org.apache.openjpa.persistence.jdbc.sqlcache;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;

import org.apache.openjpa.persistence.ExternalValues;

@Entity
@DiscriminatorValue("BOOK")
public class Book extends Merchandise {
    private String title;
    
    @ManyToMany(fetch=FetchType.EAGER)
    private Set<Author> authors;

    @ExternalValues({"SMALL=S", "MEDIUM=M", "LARGE=L"})
    private String token;
    
    public Book() {
        this("?");
        token = "MEDIUM";
    }
    
    public Book(String title) {
        super();
        setTitle(title);
    }
    
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Set<Author> getAuthors() {
        return authors;
    }

    public void addAuthor(Author a) {
        if (authors == null)
            authors = new HashSet<Author>();
        if (authors.add(a)) {
            a.addBook(this);
        }
    }
    
    public String getToken() {
        return token;
    }
    
    public void setToken(String t) {
        token = t;
    }
}
