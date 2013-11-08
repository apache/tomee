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

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import org.apache.openjpa.persistence.FetchAttribute;
import org.apache.openjpa.persistence.FetchGroup;
import org.apache.openjpa.persistence.FetchGroups;

/**
 * @author Pinaki Poddar
 *
 */

@Entity
@FetchGroups({
    @FetchGroup(name="OnlyTitle", attributes={
        @FetchAttribute(name="title")
    })
})
public class Movie {
    @Id
    private String id;
    private String title;
    private int year;
    @OneToMany(fetch=FetchType.EAGER)
    private Set<Actor> actors;
    
    protected Movie() {
        
    }
    
    public Movie(String id, String title, int year) {
        super();
        this.id = id;
        this.title = title;
        this.year = year;
    }
    
    public String getId() {
        return id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void addActor(Actor a) {
        if (actors == null)
            actors = new HashSet<Actor>();
        actors.add(a);
    }
    
    public Set<Actor> getActors() {
        return actors;
    }
    
    public int getYear() {
        return year;
    }
    
}
