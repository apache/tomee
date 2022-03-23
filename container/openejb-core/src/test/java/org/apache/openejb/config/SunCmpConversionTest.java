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

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.JAXBContextFactory;
import org.apache.openejb.jee.JaxbJavaee;
import org.apache.openejb.jee.jpa.Entity;
import org.apache.openejb.jee.jpa.EntityMappings;
import org.apache.openejb.jee.jpa.GeneratedValue;
import org.apache.openejb.jee.jpa.GenerationType;
import org.apache.openejb.jee.oejb3.OpenejbJar;
import org.custommonkey.xmlunit.DetailedDiff;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

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

    private EntityMappings convert(final String prefix) throws Exception {
        return convert(prefix + "ejb-jar.xml", prefix + "sun-ejb-jar.xml", prefix + "sun-cmp-mappings.xml", prefix + "orm.xml");
    }

    private EntityMappings convert(final String ejbJarFileName, final String sunEjbJarFileName, final String sunCmpMappingsFileName, final String expectedFileName) throws Exception {
        InputStream in = getClass().getClassLoader().getResourceAsStream(ejbJarFileName);
        final EjbJar ejbJar = (EjbJar) JaxbJavaee.unmarshalJavaee(EjbJar.class, new ByteArrayInputStream(readContent(in).getBytes()));

        // create and configure the module
        final EjbModule ejbModule = new EjbModule(getClass().getClassLoader(), "TestModule", null, ejbJar, new OpenejbJar());
        final InitEjbDeployments initEjbDeployments = new InitEjbDeployments();
        initEjbDeployments.deploy(ejbModule);
        final AppModule appModule = new AppModule(getClass().getClassLoader(), "TestModule");
        appModule.getEjbModules().add(ejbModule);

        // add the altDD
        ejbModule.getAltDDs().put("sun-cmp-mappings.xml", getClass().getClassLoader().getResource(sunCmpMappingsFileName));
        ejbModule.getAltDDs().put("sun-ejb-jar.xml", getClass().getClassLoader().getResource(sunEjbJarFileName));

        // convert the cmp declarations into jpa entity declarations
        final CmpJpaConversion cmpJpaConversion = new CmpJpaConversion();
        cmpJpaConversion.deploy(appModule);
//        EntityMappings entityMappings = cmpJpaConversion.generateEntityMappings(ejbModule);

//        // load the sun-cmp-mappings.xml file
//        String sunCmpMappingsXml = readContent(getClass().getClassLoader().getResourceAsStream(sunCmpMappingsFileName));
//        SunCmpMappings sunCmpMappings = (SunCmpMappings) JaxbSun.unmarshal(SunCmpMappings.class, new ByteArrayInputStream(sunCmpMappingsXml.getBytes()));

        // fill in the jpa entity declarations with database mappings from the sun-cmp-mappings.xml file
        final SunConversion sunConversion = new SunConversion();
//        sunCmpConversion.mergeEntityMappings(ejbModule, entityMappings);
        sunConversion.deploy(appModule);

        // compare the results to the expected results
        if (expectedFileName != null) {
            in = getClass().getClassLoader().getResourceAsStream(expectedFileName);
            final String expected = readContent(in);

            // Sun doen't really support generated primary keys, so we need to add them by hand here
            final Set<String> generatedPks = new HashSet<>(Arrays.asList("BasicCmp2", "AOBasicCmp2", "EncCmp2", "Cmp2RmiIiop"));
            final EntityMappings cmpMappings = appModule.getCmpMappings();
            for (final Entity entity : cmpMappings.getEntity()) {
                if (generatedPks.contains(entity.getName())) {
                    entity.getAttributes().getId().get(0).setGeneratedValue(new GeneratedValue(GenerationType.IDENTITY));
                }
            }
            final String actual = toString(cmpMappings);

            XMLUnit.setIgnoreWhitespace(true);
            try {
                final Diff myDiff = new DetailedDiff(new Diff(expected, actual));
                assertTrue("Files are not similar " + myDiff, myDiff.similar());
            } catch (final AssertionFailedError e) {
                assertEquals(expected, actual);
            }
        }

        return appModule.getCmpMappings();
    }


    private String toString(final EntityMappings entityMappings) throws JAXBException {
        final JAXBContext entityMappingsContext = JAXBContextFactory.newInstance(EntityMappings.class);

        final Marshaller marshaller = entityMappingsContext.createMarshaller();
        marshaller.setProperty("jaxb.formatted.output", true);

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        marshaller.marshal(entityMappings, baos);

        final String actual = new String(baos.toByteArray());
        return actual.trim();
    }


    private String readContent(InputStream in) throws IOException {
        final StringBuilder sb = new StringBuilder();
        in = new BufferedInputStream(in);
        int i = in.read();
        while (i != -1) {
            sb.append((char) i);
            i = in.read();
        }
        return sb.toString().trim();
    }
}
