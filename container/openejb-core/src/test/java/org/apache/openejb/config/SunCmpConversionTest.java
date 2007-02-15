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
package org.apache.openejb.config;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import junit.framework.TestCase;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.JaxbJavaee;
import org.apache.openejb.jee.oejb3.OpenejbJar;
import org.apache.openejb.jee.jpa.EntityMappings;
import org.apache.openejb.jee.jpa.Entity;
import org.apache.openejb.jee.jpa.GeneratedValue;
import org.apache.openejb.jee.jpa.GenerationType;

/**
 * @version $Rev$ $Date$
 */
public class SunCmpConversionTest extends TestCase {
    public void testItests22() throws Exception {
        convert("convert/oej2/cmp/itest-2.2/itest-2.2-");
    }

//    public void testDaytrader() throws Exception {
//        convert("convert/oej2/cmp/daytrader/daytrader-");
//    }
//
    public void testOneToOne() throws Exception {
        convert("convert/oej2/cmp/onetoone/simplepk/");
    }

    public void testOneToOneUni() throws Exception {
        convert("convert/oej2/cmp/onetoone/simplepk/unidirectional-");
    }

    public void testOneToMany() throws Exception {
        convert("convert/oej2/cmp/onetomany/simplepk/");
    }

    public void testOneToManyUni() throws Exception {
        convert("convert/oej2/cmp/onetomany/simplepk/one-unidirectional-");
    }

    public void testManyToOneUni() throws Exception {
        convert("convert/oej2/cmp/onetomany/simplepk/many-unidirectional-");
    }

    public void testManyToMany() throws Exception {
        convert("convert/oej2/cmp/manytomany/simplepk/");
    }

    public void testManyToManyUni() throws Exception {
        convert("convert/oej2/cmp/manytomany/simplepk/unidirectional-");
    }

    private EntityMappings convert(String prefix) throws Exception {
        return convert(prefix + "ejb-jar.xml", prefix + "sun-ejb-jar.xml", prefix + "sun-cmp-mappings.xml", prefix + "orm.xml");
    }

    private EntityMappings convert(String ejbJarFileName, String sunEjbJarFileName, String sunCmpMappingsFileName, String expectedFileName) throws Exception {
        InputStream in = getClass().getClassLoader().getResourceAsStream(ejbJarFileName);
        EjbJar ejbJar = (EjbJar) JaxbJavaee.unmarshal(EjbJar.class, new ByteArrayInputStream(readContent(in).getBytes()));

        // create and configure the module
        EjbModule ejbModule = new EjbModule(getClass().getClassLoader(), "TestModule", ejbJarFileName, ejbJar, new OpenejbJar());
        AutoConfig autoConfig = new AutoConfig();
        autoConfig.deploy(ejbModule, new HashMap<String,String>());
        AppModule appModule = new AppModule(getClass().getClassLoader(), "TestModule");
        appModule.getEjbModules().add(ejbModule);

        // add the altDD
        ejbModule.getAltDDs().put("sun-cmp-mappings.xml", getClass().getClassLoader().getResource(sunCmpMappingsFileName));
        ejbModule.getAltDDs().put("sun-ejb-jar.xml", getClass().getClassLoader().getResource(sunEjbJarFileName));

        // convert the cmp declarations into jpa entity declarations
        CmpJpaConversion cmpJpaConversion = new CmpJpaConversion();
        cmpJpaConversion.deploy(appModule);
//        EntityMappings entityMappings = cmpJpaConversion.generateEntityMappings(ejbModule);

//        // load the sun-cmp-mappings.xml file
//        String sunCmpMappingsXml = readContent(getClass().getClassLoader().getResourceAsStream(sunCmpMappingsFileName));
//        SunCmpMappings sunCmpMappings = (SunCmpMappings) JaxbSun.unmarshal(SunCmpMappings.class, new ByteArrayInputStream(sunCmpMappingsXml.getBytes()));

        // fill in the jpa entity declarations with database mappings from the sun-cmp-mappings.xml file
        SunConversion sunConversion = new SunConversion();
//        sunCmpConversion.mergeEntityMappings(ejbModule, entityMappings);
        sunConversion.deploy(appModule);

        // compare the results to the expected results (direct text comparison)
        if (expectedFileName != null) {
            in = getClass().getClassLoader().getResourceAsStream(expectedFileName);
            String expected = readContent(in);

            // Sun doen't really support generated primary keys, so we need to add them by hand here
            Set<String> generatedPks = new HashSet<String>(Arrays.asList("BasicCmp2", "AOBasicCmp2", "EncCmp2", "Cmp2RmiIiop"));
            EntityMappings cmpMappings = appModule.getCmpMappings();
            for (Entity entity : cmpMappings.getEntity()) {
                if (generatedPks.contains(entity.getName())) {
                    entity.getAttributes().getId().get(0).setGeneratedValue(new GeneratedValue(GenerationType.IDENTITY));
                }
            }
            String actual = toString(cmpMappings);
            assertEquals(expected, actual);
        }

        return appModule.getCmpMappings();
    }


    private String toString(EntityMappings entityMappings) throws JAXBException {
        JAXBContext entityMappingsContext = JAXBContext.newInstance(EntityMappings.class);

        Marshaller marshaller = entityMappingsContext.createMarshaller();
        marshaller.setProperty("jaxb.formatted.output", true);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        marshaller.marshal(entityMappings, baos);

        String actual = new String(baos.toByteArray());
        return actual.trim();
    }


    private String readContent(InputStream in) throws IOException {
        StringBuffer sb = new StringBuffer();
        in = new BufferedInputStream(in);
        int i = in.read();
        while (i != -1) {
            sb.append((char) i);
            i = in.read();
        }
        return sb.toString().trim();
    }
}
