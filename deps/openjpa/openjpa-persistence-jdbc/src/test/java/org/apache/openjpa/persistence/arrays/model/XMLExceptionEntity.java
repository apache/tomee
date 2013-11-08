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
package org.apache.openjpa.persistence.arrays.model;

import java.util.ArrayList;

/**
 * Entity of questionable real-world value. Intended to test the ability to persist an array of serializable types (in
 * this case exceptions) as a Lob.
 */
public class XMLExceptionEntity  {
    private int id;

    private ArrayList<Exception> exceptions;

    // Element collection does not work with Exceptions
    private ArrayList<String> elemCollExceptions;

    private ArrayList<Exception> persCollExceptions;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public ArrayList<Exception> getExceptions() {
        return exceptions;
    }

    public void setExceptions(ArrayList<Exception> exceptions) {
        this.exceptions = exceptions;
    }

    public ArrayList<String> getElemCollExceptions() {
        return elemCollExceptions;
    }

    public void setElemCollExceptions(ArrayList<String> elemCollExceptions) {
        this.elemCollExceptions = elemCollExceptions;
    }

    public ArrayList<Exception> getPersCollExceptions() {
        return persCollExceptions;
    }

    public void setPersCollExceptions(ArrayList<Exception> persCollExceptions) {
        this.persCollExceptions = persCollExceptions;
    }
}
