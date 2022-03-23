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

import junit.framework.TestCase;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.jee.JAXBContextFactory;
import org.apache.openejb.jee.jpa.EntityMappings;
import org.apache.openejb.jee.oejb2.GeronimoEjbJarType;
import org.apache.openejb.jee.oejb3.OpenejbJar;
import org.apache.openejb.loader.IO;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.ElementNameAndAttributeQualifier;
import org.custommonkey.xmlunit.XMLUnit;
import org.xml.sax.SAXException;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;

/**
 * @version $Rev$ $Date$
 */
public class OpenEjb2ConversionTest extends TestCase {

    public void testSimple() throws Exception {
        final String prefix = "convert/oej2/simple/";

        final AppModule appModule = deploy(prefix);

        // compare the results to the expected results
        final EjbModule ejbModule = appModule.getEjbModules().get(0);

        assertJaxb(prefix + "openejb-jar-expected.xml", ejbModule.getOpenejbJar(), OpenejbJar.class);
        assertJaxb(prefix + "geronimo-openejb.xml", ejbModule.getAltDDs().get("geronimo-openejb.xml"), GeronimoEjbJarType.class);
    }

    public void testItests22() throws Exception {
        convertCmp("convert/oej2/cmp/itest-2.2/itest-2.2-");
    }

    public void testItests22Pojo() throws Exception {
        convertCmp("convert/oej2/cmp/itest-2.2/itest-2.2-pojo-");
    }

    public void testDaytrader() throws Exception {
        convertCmp("convert/oej2/cmp/daytrader/daytrader-");
    }

    public void testOneToOne() throws Exception {
        convertCmp("convert/oej2/cmp/onetoone/simplepk/");
    }

    public void testOneToOneUni() throws Exception {
        convertCmp("convert/oej2/cmp/onetoone/simplepk/unidirectional-");
    }

    public void testOneToMany() throws Exception {
        convertCmp("convert/oej2/cmp/onetomany/simplepk/");
    }

    public void testOneToManyUni() throws Exception {
        convertCmp("convert/oej2/cmp/onetomany/simplepk/one-unidirectional-");
    }

    public void testManyToOneUni() throws Exception {
        convertCmp("convert/oej2/cmp/onetomany/simplepk/many-unidirectional-");
    }

    public void testManyToMany() throws Exception {
        convertCmp("convert/oej2/cmp/manytomany/simplepk/");
    }

    public void testManyToManyUni() throws Exception {
        convertCmp("convert/oej2/cmp/manytomany/simplepk/unidirectional-");
    }

    private void convertCmp(final String prefix) throws Exception {
        final AppModule appModule = deploy(prefix);

        // compare the results to the expected results
        assertJaxb(prefix + "orm.xml", appModule.getCmpMappings(), EntityMappings.class);
    }

    private void assertJaxb(final String expectedFile, final Object object, final Class<?> type) throws IOException, JAXBException, SAXException {

        assertSame(type, object.getClass());

        final String actual = toString(object, type);
        final boolean nw = XMLUnit.getNormalizeWhitespace();
        final boolean n = XMLUnit.getNormalize();
        InputStreamReader isr = null;

        try {

            XMLUnit.setNormalizeWhitespace(true);
            XMLUnit.setNormalize(true);

            isr = new InputStreamReader(IO.read(getClass().getClassLoader().getResource(expectedFile)));
            final org.w3c.dom.Document actualDoc = XMLUnit.buildDocument(XMLUnit.newTestParser(), new StringReader(actual));
            final org.w3c.dom.Document expectedDoc = XMLUnit.buildDocument(XMLUnit.newControlParser(), isr);

            final Diff myDiff = new Diff(expectedDoc, actualDoc);
            myDiff.overrideElementQualifier(new ElementNameAndAttributeQualifier());
            assertTrue("Files are similar " + myDiff, myDiff.similar());
        } finally {
            XMLUnit.setNormalizeWhitespace(nw);
            XMLUnit.setNormalize(n);

            if (null != isr) {
                isr.close();
            }
        }
    }

    private AppModule deploy(final String prefix) throws OpenEJBException {
        return deploy(prefix + "ejb-jar.xml", prefix + "openejb-jar.xml");
    }

    private AppModule deploy(final String ejbJarFileName, final String openejbJarFileName) throws OpenEJBException {
        // create and configure the module
        final EjbModule ejbModule = new EjbModule(getClass().getClassLoader(), "TestModule", null, null, null);
        final AppModule appModule = new AppModule(getClass().getClassLoader(), "TestModule");
        appModule.getEjbModules().add(ejbModule);

        // add the altDD
        ejbModule.getAltDDs().put("ejb-jar.xml", getClass().getClassLoader().getResource(ejbJarFileName));
        ejbModule.getAltDDs().put("openejb-jar.xml", getClass().getClassLoader().getResource(openejbJarFileName));

        final DynamicDeployer[] deployers = {new ReadDescriptors(), new InitEjbDeployments(), new CmpJpaConversion(), new OpenEjb2Conversion()};

        for (final DynamicDeployer deployer : deployers) {
            deployer.deploy(appModule);
        }
        return appModule;
    }

    private String toString(final Object object, final Class<?> type) throws JAXBException {
        final JAXBContext jaxbContext = JAXBContextFactory.newInstance(type);

        final Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty("jaxb.formatted.output", true);

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        marshaller.marshal(object, baos);

        final String actual = new String(baos.toByteArray());
        return actual.trim();
    }
}
