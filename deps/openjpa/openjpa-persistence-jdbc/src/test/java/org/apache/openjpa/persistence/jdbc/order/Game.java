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
package org.apache.openjpa.persistence.jdbc.order;

import java.util.Collection;
import java.util.List;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.OrderColumn;

@Entity
@Table(name="OCGame")
public class Game {

    @Id
    @GeneratedValue(generator="uuid-type4-hex")
    private String id;    
    
    @ManyToMany
    @OrderColumn(name="playerOrder")
    private List<Player> playedIn;
    
    @ElementCollection
    @OrderColumn(name="dateOrder")
    private List<java.sql.Date> rainDates;
    
    @ElementCollection
    @OrderColumn(name="inningOrder")
    private Collection<Inning> innings;
    
    public Game() {
    }
    
    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
    
    public void setPlayedIn(List<Player> playedIn) {
        this.playedIn = playedIn;
    }

    public List<Player> getPlayedIn() {
        return playedIn;
    }
 
    public boolean equals(Object obj) {
        if (obj instanceof Game) {
            Game game = (Game)obj;
            return getId().equals(game.getId());
        }
        return false;
    }

    public void setRainDates(List<java.sql.Date> rainDates) {
        this.rainDates = rainDates;
    }

    public List<java.sql.Date> getRainDates() {
        return rainDates;
    }

    public void setInnings(Collection<Inning> innings) {
        this.innings = innings;
    }

    public Collection<Inning> getInnings() {
        return innings;
    }
}
