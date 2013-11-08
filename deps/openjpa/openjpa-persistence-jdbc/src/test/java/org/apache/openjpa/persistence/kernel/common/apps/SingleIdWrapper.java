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



import javax.persistence.Entity;

import org.apache.openjpa.util.IntId;

@Entity
public class SingleIdWrapper implements SingleId {

    private Integer pk;
    private String str;

    public boolean correctIdClass(Class c) {
        return IntId.class.equals(c);
    }

    public String getPkString() {
        return pk == null ? "null" : pk.toString();
    }

    public int getPk() {
        return pk.intValue();
    }

    public void setPk(int i) {
        pk = new Integer(i);
    }

    public String getString() {
        return str;
    }

    public void setString(String s) {
        str = s;
    }

    public static void main(String[] args) {
        SingleIdWrapper wrap = new SingleIdWrapper();
    }
}
