/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.examples.enventries;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import java.io.File;
import java.net.InetAddress;
import java.net.URI;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * In addition to the standard env-entry types (String, Integer, Long, Short, Byte, Boolean, Double, Float, Character)
 * OpenEJB supports many other types.
 *
 * This bean has just about every flavor of env-entry OpenEJB supports.
 *
 * @version $Rev$ $Date$
 */
@Stateless
public class StratocasterImpl implements Stratocaster {

    @Resource(name = "myClass")
    private Class myClass;
    @Resource(name = "myDate")
    private Date myDate;
    @Resource(name = "myFile")
    private File myFile;
    @Resource(name = "myInetAddress")
    private InetAddress myInetAddress;
    @Resource(name = "myList")
    private List myList;
    @Resource(name = "myMap")
    private Map myMap;
    @Resource(name = "myURI")
    private URI myURI;
    @Resource(name = "myURL")
    private URL myURL;

    public Class getMyClass() {
        return myClass;
    }

    public Date getMyDate() {
        return myDate;
    }

    public File getMyFile() {
        return myFile;
    }

    public InetAddress getMyInetAddress() {
        return myInetAddress;
    }

    public List getMyList() {
        return myList;
    }

    public Map getMyMap() {
        return myMap;
    }

    public URI getMyURI() {
        return myURI;
    }

    public URL getMyURL() {
        return myURL;
    }

}
