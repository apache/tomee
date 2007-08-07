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

import junit.framework.TestCase;

import javax.naming.InitialContext;
import javax.naming.Context;
import java.util.Properties;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.io.File;
import java.net.URI;
import java.net.URL;
import java.net.InetAddress;
import java.text.DateFormat;

/**
 * @version $Rev$ $Date$
 */
public class StratocasterTest extends TestCase {

    private InitialContext initialContext;

    protected void setUp() throws Exception {
        Properties properties = new Properties();
        properties.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.openejb.client.LocalInitialContextFactory");
        properties.setProperty("openejb.deployments.classpath.include", ".*expanded-env-entries.*");

        initialContext = new InitialContext(properties);
    }

    public void test() throws Exception {
        Stratocaster stratocaster = (Stratocaster) initialContext.lookup("StratocasterImplBusinessLocal");


        assertEquals("Stratocaster.getMyClass()", Stratocaster.class, stratocaster.getMyClass());

        Date date = DateFormat.getDateInstance().parse("Jan 1, 1954");
        assertEquals("Stratocaster.getMyDate()", date, stratocaster.getMyDate());

        File file = new File("/tmp/play-history.txt").getCanonicalFile();
        assertEquals("Stratocaster.getMyFile()", file, stratocaster.getMyFile());

        InetAddress host = InetAddress.getByName("localhost");
        assertEquals("Stratocaster.getMyInetAddress()", host, stratocaster.getMyInetAddress());

        List<String> list = new LinkedList();
        list.add("Stevie Ray Vaughan");
        list.add("Eric Johnson");
        list.add("Mark Knopfler");
        list.add("Buddy Guy");
        assertEquals("Stratocaster.getMyList()", list, stratocaster.getMyList());

        Map<String,String> map = new LinkedHashMap();
        map.put("color", "3-Color Sunburst");
        map.put("neck", "maple");
        map.put("fretboard", "African rosewood");
        map.put("pickups", "Texas Special");
        assertEquals("Stratocaster.getMyMap()", map, stratocaster.getMyMap());

        assertEquals("Stratocaster.getMyURI()", new URI("game://guitarheroII/?mode=expert"), stratocaster.getMyURI());

        assertEquals("Stratocaster.getMyURL()", new URL("http://www.fender.com/"), stratocaster.getMyURL());


    }
}
