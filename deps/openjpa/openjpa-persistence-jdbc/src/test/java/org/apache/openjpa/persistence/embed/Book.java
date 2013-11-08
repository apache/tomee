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
package org.apache.openjpa.persistence.embed;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.*;

@Entity
@Table(name="BK_EMD")
public class Book {
	@Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private int id;
	
    private long isbn;
	
    @ElementCollection
    @CollectionTable(name="listing")
    protected Set<Listing> listings = new HashSet<Listing>();

    public Book(){}

    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
	public Book(long isbn){
		this.isbn=isbn;
	}
	
	public long getIsbc() {
	    return isbn;
	}
	
	public void setIsbc(long isbn) {
	    this.isbn = isbn;
	}
	
	public Set<Listing> getListings() {
	    return listings;
	}
	
	public void setListings(Set<Listing> listings) {
	    this.listings = listings;
	}
	
	public void addListing(Listing l){
		listings.add(l);
	}
	
	
    public String toString(){
    	String res ="Book isbn: " + isbn + "\nListings: ";
    	for(Listing l : listings){
    		res+="\t"+l.toString() + "\n";
    	}
    	return res.substring(0, res.length()-2);
    }
    
}
