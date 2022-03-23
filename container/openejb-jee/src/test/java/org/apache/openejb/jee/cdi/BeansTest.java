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
package org.apache.openejb.jee.cdi;

import org.apache.openejb.jee.Beans;
import org.apache.openejb.jee.JaxbJavaee;
import org.junit.Test;
import org.xml.sax.SAXException;

import jakarta.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class BeansTest {
    @Test
    public void readEmpty10() throws JAXBException, SAXException, ParserConfigurationException {
        final Beans b = read("<beans xmlns=\"http://java.sun.com/xml/ns/javaee\"\n" +
                "       xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                "       xsi:schemaLocation=\"http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/beans_1_0.xsd\" />");
        assertNotNull(b);
    }

    @Test
    public void read10() throws Exception {
        final Beans b = read(
                "<beans\n" +
                "   xmlns=\"http://xmlns.jcp.org/xml/ns/javaee\"\n" +
                "   xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                "   version=\"1.0\"\n" +
                "   xsi:schemaLocation=\"\n" +
                "      http://xmlns.jcp.org/xml/ns/javaee\n" +
                "      http://xmlns.jcp.org/xml/ns/javaee http://xmlns.jcp.org/xml/ns/javaee/beans_2_0.xsd\">\n" +
                "   <interceptors>\n" +
                "      <class>org.mycompany.myapp.TransactionInterceptor</class>\n" +
                "   </interceptors>\n" +
                "   <alternatives>\n" +
                "         <stereotype>org.mycompany.myapp.Staging</stereotype>\n" +
                "   </alternatives>" +
                "   <decorators>\n" +
                "      <class>org.mycompany.myfwk.TimestampLogger</class>\n" +
                "   </decorators>" +
                "   <trim/>" +
                "</beans>");
        assertNotNull(b);
        assertEquals(1, b.getInterceptors().size());
        assertEquals("org.mycompany.myapp.TransactionInterceptor", b.getInterceptors().iterator().next());
        assertEquals(0, b.getAlternativeClasses().size());
        assertEquals(1, b.getAlternativeStereotypes().size());
        assertEquals("org.mycompany.myapp.Staging", b.getAlternativeStereotypes().iterator().next());
        assertEquals(1, b.getDecorators().size());
        assertEquals("org.mycompany.myfwk.TimestampLogger", b.getDecorators().iterator().next());
        assertEquals("1.0", b.getVersion());
        assertTrue(b.isTrim());
    }

    @Test
    public void read11() throws Exception {
        final Beans b = read(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<beans xmlns=\"http://xmlns.jcp.org/xml/ns/javaee\" version=\"1.1\">\n" +
                        "    <scan>\n" +
                        "        <exclude name=\"com.acme.swing.**\" />\n" +
                        "        <exclude name=\"com.acme.gwt.**\">\n" +
                        "            <if-class-not-available name=\"com.google.GWT\"/>\n" +
                        "        </exclude>\n" +
                        "        <exclude name=\"com.acme.verbose.*\">\n" +
                        "            <if-system-property name=\"verbosity\" value=\"low\"/>\n" +
                        "        </exclude>\n" +
                        "        <exclude name=\"com.acme.jsf.**\">\n" +
                        "            <if-class-available name=\"org.apache.wicket.Wicket\"/>\n" +
                        "            <if-system-property name=\"viewlayer\"/>\n" +
                        "        </exclude>\n" +
                        "    </scan>\n" +
                        "</beans>");
        assertNotNull(b);
        assertEquals("1.1", b.getVersion());
        assertNotNull(b.getScan());
        assertFalse(b.isTrim());
        final List<Beans.Scan.Exclude> excludeList = b.getScan().getExclude();
        assertNotNull(excludeList);
        assertEquals(4, excludeList.size());
        for (int i = 0; i < 4; i++) {
            final Beans.Scan.Exclude exclude = excludeList.get(i);
            switch (i) {
                case 0:
                    assertEquals("com.acme.swing.**", exclude.getName());
                    assertEquals(0, exclude.getIfClassAvailableOrIfClassNotAvailableOrIfSystemProperty().size());
                    break;
                case 1:
                    assertEquals("com.acme.gwt.**", exclude.getName());
                    assertEquals(1, exclude.getIfClassAvailableOrIfClassNotAvailableOrIfSystemProperty().size());
                    assertEquals("com.google.GWT", Beans.Scan.Exclude.IfNotAvailableClassCondition.class.cast(exclude.getIfClassAvailableOrIfClassNotAvailableOrIfSystemProperty().iterator().next()).getName());
                    break;
                case 2:
                    assertEquals("com.acme.verbose.*", exclude.getName());
                    assertEquals(1, exclude.getIfClassAvailableOrIfClassNotAvailableOrIfSystemProperty().size());
                    final Beans.Scan.Exclude.IfSystemProperty systemProperty = Beans.Scan.Exclude.IfSystemProperty.class.cast(exclude.getIfClassAvailableOrIfClassNotAvailableOrIfSystemProperty().iterator().next());
                    assertEquals("verbosity", systemProperty.getName());
                    assertEquals("low", systemProperty.getValue());
                    break;
                case 3:
                    assertEquals("com.acme.jsf.**", exclude.getName());
                    assertEquals(2, exclude.getIfClassAvailableOrIfClassNotAvailableOrIfSystemProperty().size());
                    final Iterator<Object> iterator = exclude.getIfClassAvailableOrIfClassNotAvailableOrIfSystemProperty().iterator();
                    assertEquals("org.apache.wicket.Wicket", Beans.Scan.Exclude.IfAvailableClassCondition.class.cast(iterator.next()).getName());
                    final Beans.Scan.Exclude.IfSystemProperty ifSystemProperty = Beans.Scan.Exclude.IfSystemProperty.class.cast(iterator.next());
                    assertEquals("viewlayer", ifSystemProperty.getName());
                    assertNull(ifSystemProperty.getValue());
                    break;
            }
        }
    }

    private Beans read(final String value) throws ParserConfigurationException, SAXException, JAXBException {
        return (Beans) JaxbJavaee.unmarshalJavaee(Beans.class, new ByteArrayInputStream(value.getBytes()));
    }
}
