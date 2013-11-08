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
import javax.persistence.IdClass;
import javax.persistence.ManyToOne;
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
@IdClass(BookId.class)
@VersionColumn
public class Book implements Serializable {
    @Id
    @Column(name="BOOK_NAME", nullable = false)
    private String name;
    
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "book")
    private Set<Page> pages = new HashSet<Page>();
    
    @Id
    @Column(nullable = false)
    @ManyToOne(cascade = CascadeType.MERGE)
    private Library library;
    
    private String author;
    
	public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public Library getLibrary() {
        return library;
    }

    public void setLibrary(Library library) {
        this.library = library;
    }

    public Page getPage(int n) {
        for (Page p: pages) {
            if (p.getNumber() == n) {
                return p;
            }
        }
        return null;
    }
    
    public void addPage(Page p) {
        p.setBook(this);
        pages.add(p);
    }
    
	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

    public boolean equals(Object o) {
        if (!(o instanceof Book)) {
            return false;
        }
        
        Book other = (Book)o;
        
        if (!getName().equals(other.getName())) {
            return false;
        }
        
        return true;
    }

    public int hashCode() {
        return getName().hashCode();
    }
}
