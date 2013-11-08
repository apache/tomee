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

@Entity
@DiscriminatorValue("AUTHOR")
public class Author extends Person {
    private String name;
    
    @ManyToMany(fetch=FetchType.LAZY)
    private Set<Book> books;
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Author() {
        super();
    }

    public Author(String firstName, String lastName, short age, int yob) {
        super(firstName, lastName, age, yob);
    }

    public Set<Book> getBooks() {
        return books;
    }
    
    public void addBook(Book b) {
        if (books == null)
            books = new HashSet<Book>();
        if (books.add(b)) {
            b.addAuthor(this);
        }
    }
}
