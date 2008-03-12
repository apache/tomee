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
package org.superbiz.enventries;

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
 */
//START SNIPPET: code
@Stateless
public class StratocasterImpl implements Stratocaster {


    @Resource(name = "pickups")
    private List<Pickup> pickups;

    @Resource(name = "style")
    private Style style;

    @Resource(name = "dateCreated")
    private Date dateCreated;

    @Resource(name = "guitarStringGuages")
    private Map<String, Float> guitarStringGuages;

    @Resource(name = "certificateOfAuthenticity")
    private File certificateOfAuthenticity;

    public Date getDateCreated() {
        return dateCreated;
    }

    /**
     * Gets the guage of the electric guitar strings
     * used in this guitar.
     * @param string
     * @return
     */
    public float getStringGuage(String string){
        return guitarStringGuages.get(string);
    }

    public List<Pickup> getPickups() {
        return pickups;
    }

    public Style getStyle() {
        return style;
    }

    public File getCertificateOfAuthenticity() {
        return certificateOfAuthenticity;
    }
}
//END SNIPPET: code
