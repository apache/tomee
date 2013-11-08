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
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;

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
@Table(name="DI_LIBRARY1")
@VersionColumn
public class Library1 implements Serializable {
    @Id
    @Column(name="LIBRARY_NAME", nullable = false)
    private String name;
    
    @OneToMany(fetch=FetchType.EAGER, cascade = CascadeType.ALL, mappedBy = "library")
    @OrderBy(value = "bid.library ASC, bid.name ASC")
    private Set<Book1> books = new HashSet<Book1>();
    
    private String location;
    
	public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public Set<Book1> getBooks() {
		return books;
	}

    public Book1 getBook(BookId1 bid) {
        for (Book1 b: books) {
            if (b.getBid().equals(bid)) {
                return b;
            }
        }
        
        return null;
    }

    public void addBook(Book1 book) {
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
        if (!(o instanceof Library1)) {
            return false;
        }
        
        Library1 other = (Library1)o;
        
        if (!getName().equals(other.getName())) {
            return false;
        }
        
        return true;
    }

    public int hashCode() {
        return getName().hashCode();
    }
}
