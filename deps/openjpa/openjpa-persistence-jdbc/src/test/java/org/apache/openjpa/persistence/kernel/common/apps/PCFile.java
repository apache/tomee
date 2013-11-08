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
package org.apache.openjpa.persistence.kernel.common.apps;

/**
 * @author <A HREF="mailto:pinaki.poddar@gmail.com>Pinaki Poddar</A>
 */

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Column;

@Entity
public class PCFile {

    @Column(name="name_col")
    private String _name;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    private PCDirectory _dir;

    /**
     *
     */
    protected PCFile() {
        super();
    }

    PCFile(PCDirectory dir, String name) {
        super();
        if (dir == null)
            throw new NullPointerException("null directory");
        if (name == null || name.trim().length() == 0)
            throw new NullPointerException("null name");

        _dir = dir;
        _name = name;
    }

    public PCDirectory getDir() {
        return _dir;
    }

    public String getName() {
        return _name;
    }
}
