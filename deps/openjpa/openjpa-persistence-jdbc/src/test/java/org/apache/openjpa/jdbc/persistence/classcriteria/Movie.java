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
@DiscriminatorValue(value = "MOVIE")
public class Movie extends Item {
    private static final long serialVersionUID = 5263476520279196994L;

    @Column(name = "DURATION")
    private Integer duration;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @Column(name = "ARTIST")
    private Artist artist;

    public Movie() {
        super();
    }

    public Movie(String title) {
        super(title);
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(int d) {
        if (d < 0) {
            throw new IllegalArgumentException("Invalid duration " + d + " for " + this);
        }

        duration = d;
    }

    public Artist getArtist() {
        return artist;
    }

    public void setArtist(Artist artist) {
        this.artist = artist;
    }

}
