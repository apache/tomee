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
package org.apache.openjpa.persistence.enhance.identity;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import org.apache.openjpa.persistence.jdbc.VersionColumn;

/**
 * Entity used to test compound primary keys using entity as relationship to 
 * more than one level.
 * 
 * Test case and domain classes were originally part of the reported issue
 * <A href="https://issues.apache.org/jira/browse/OPENJPA-207">OPENJPA-207</A>
 *  
 * @author Jeffrey Blattman
 * @author Pinaki Poddar
 *
 */
@Entity
@VersionColumn
public class Library implements Serializable {
    @Id
    @Column(name="LIBRARY_NAME", nullable = false)
    private String name;
    
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "library")
    private Set<Book> books = new HashSet<Book>();
    
    private String location;
    
	public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public Set<Book> getBooks() {
		return books;
	}

    public Book getBook(String name) {
        for (Book b: books) {
            if (b.getName().equals(name)) {
                return b;
            }
        }
        
        return null;
    }

    public void addBook(Book book) {
        book.setLibrary(this);
        books.add(book);
    }
    
	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

    public boolean equals(Object o) {
        if (!(o instanceof Library)) {
            return false;
        }
        
        Library other = (Library)o;
        
        if (!getName().equals(other.getName())) {
            return false;
        }
        
        return true;
    }

    public int hashCode() {
        return getName().hashCode();
    }
}
