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
package org.apache.openjpa.persistence.jdbc.annotations;


import java.awt.Point;

import javax.persistence.*;

import org.apache.openjpa.persistence.*;
import org.apache.openjpa.persistence.jdbc.*;

@MappedSuperclass
public abstract class NonstandardMappingMappedSuper4 {

    @Persistent
    @Strategy("PointHandler")
    @Column(name="my_point")
    private Point point;
    
    public Point getPoint() { 
        return point; 
    }
    
    public void setPoint(Point point) { 
        this.point = point; 
    }
   
}
