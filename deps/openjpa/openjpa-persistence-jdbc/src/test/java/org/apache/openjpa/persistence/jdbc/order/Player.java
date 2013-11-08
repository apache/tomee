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

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.OrderColumn;

@Entity
public class Player extends Person {
    
    private int playerNumber;

    @ManyToMany(mappedBy="playersTrained")
    private List<Trainer> trainers;

    @ManyToMany(mappedBy="playedIn")
    @OrderColumn(name="playedInOrder")
    private List<Game> gamesPlayedIn;
    
    public Player() {        
    }
    
    public Player(String name, int number) {
        setName(name);
        this.playerNumber = number;
    }

    public void setPlayerNumber(int number) {
        this.playerNumber = number;
    }

    public int getPlayerNumber() {
        return playerNumber;
    }    

    public void setTrainers(List<Trainer> trainers) {
        this.trainers = trainers;
    }

    public List<Trainer> getTrainers() {
        return trainers;
    }

    public boolean equals(Object obj) {
        if (obj instanceof Player) {
            Player player = (Player)obj;
            return super.equals(obj) &&
                player.getPlayerNumber() == playerNumber;
        }
        return false;
    }

    public void setGamesPlayedIn(List<Game> gamesPlayedIn) {
        this.gamesPlayedIn = gamesPlayedIn;
    }

    public List<Game> getGamesPlayedIn() {
        return gamesPlayedIn;
    }
}
