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

import javax.persistence.Embeddable;

@Embeddable
public class Inning {

    private int inningNumber;
    
    private int hitsHome;
    
    private int hitsAway;
    
    public Inning() {        
    }

    public Inning(int num, int hh, int ha) {        
        inningNumber = num;
        hitsHome = hh;
        hitsAway = ha;
    }

    public void setInningNumber(int number) {
        this.inningNumber = number;
    }

    public int getInningNumber() {
        return inningNumber;
    }

    public void setHitsHome(int hitsHome) {
        this.hitsHome = hitsHome;
    }

    public int getHitsHome() {
        return hitsHome;
    }

    public void setHitsAway(int hitsAway) {
        this.hitsAway = hitsAway;
    }

    public int getHitsAway() {
        return hitsAway;
    } 
    
    public boolean equals(Object obj) {
        if (obj instanceof Inning) {
            Inning in = (Inning)obj;
            return getInningNumber() == in.getInningNumber() &&
                getHitsHome() == in.getHitsHome() &&
                getHitsAway() == in.getHitsAway();
        }
        return false;
    }
}
