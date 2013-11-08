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

package demo;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import org.apache.openjpa.persistence.FetchAttribute;
import org.apache.openjpa.persistence.FetchGroup;
import org.apache.openjpa.persistence.FetchGroups;

/**
 * A persistent entity with singular and plural association.
 * 
 * @author Pinaki Poddar
 *
 */
@Entity
@FetchGroups({
    @FetchGroup(name="OnlyName", attributes={
        @FetchAttribute(name="firstName"),
        @FetchAttribute(name="lastName")
    })
})
public class Actor {
    public static enum Gender {Male, Female}; 
    @Id
    private String id;
    private String firstName;
    private String lastName;
    private Gender gender;
    private Date dob;
    @OneToOne
    private Actor partner;
    @OneToMany
    private Set<Movie> movies;
    
    protected Actor() {
        
    }
    
    public Actor(String id, String firstName, String lastName, Gender gender, Date dob) {
        super();
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.gender = gender;
        this.dob = dob;
    }

    public String getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public Gender getGender() {
        return gender;
    }

    public Date getDob() {
        return dob;
    }
    public Actor getPartner() {
        return partner;
    }

    public void setPartner(Actor partner) {
        this.partner = partner;
    }

    public Set<Movie> getMovies() {
        return movies;
    }

    public void addMovie(Movie movie) {
        if (movies == null)
            movies = new HashSet<Movie>();
        movies.add(movie);
    }
}
