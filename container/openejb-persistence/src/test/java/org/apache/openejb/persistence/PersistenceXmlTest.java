/**
 *
 * Copyright 2006 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.persistence;


import junit.framework.TestCase;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.Marshaller;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.net.URL;

import org.apache.openejb.persistence.dd.Persistence;

/**
 * @version $Revision$ $Date$
 */
public class PersistenceXmlTest extends TestCase {

    /**
     * TODO: What does the test test out? The comparision using assertEquals doesn't seem to work well with xml files.
     *
     * @throws Exception
     */
    public void testAll() throws Exception {
//        Persistence persistence = new Persistence();
//        persistence.setVersion("1.0");
//        Persistence.PersistenceUnit persistenceUnit = new Persistence.PersistenceUnit();
//        persistenceUnit.setDescription("description");
//        persistenceUnit.setExcludeUnlistedClasses(true);
//        persistenceUnit.setJtaDataSource("jtadatasource");
//        persistenceUnit.setName("name");
//        persistenceUnit.setNonJtaDataSource("nonjtadatasource");
//        persistenceUnit.setProvider("org.acme.Provider");
//        persistenceUnit.setTransactionType(TransactionType.JTA);
//        persistenceUnit.getClazz().add("org.acme.Person");
//        persistenceUnit.getClazz().add("org.acme.Animal");
//        persistenceUnit.getJarFile().add("jarfile1");
//        persistenceUnit.getJarFile().add("jarfile2");
//        persistenceUnit.getMappingFile().add("mappingfile1");
//        persistenceUnit.getMappingFile().add("mappingfile2");
//
//        Persistence.PersistenceUnit.Properties properties = new Persistence.PersistenceUnit.Properties();
//        properties.setProperty("foo","oof");
//        properties.setProperty("bar","rab");
//        properties.setProperty("baz","zab");
//        persistenceUnit.setProperties(properties);
//
//        persistenceUnit.setProperties(properties);
//        persistence.getPersistenceUnit().add(persistenceUnit);

        JAXBContext ctx = JAXBContext.newInstance(Persistence.class);
        Unmarshaller unmarshaller = ctx.createUnmarshaller();

        URL resource = this.getClass().getClassLoader().getResource("persistence-example.xml");
        InputStream in = resource.openStream();
        java.lang.String expected = readContent(in);

        Persistence element =  (Persistence) unmarshaller.unmarshal(new ByteArrayInputStream(expected.getBytes()));
        unmarshaller.setEventHandler(new TestValidationEventHandler());
        System.out.println("unmarshalled");

        Marshaller marshaller = ctx.createMarshaller();
        marshaller.setProperty("jaxb.formatted.output", true);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        marshaller.marshal(element, baos);

        java.lang.String actual = new java.lang.String(baos.toByteArray());

        assertEquals(expected, actual);
    }

    private java.lang.String readContent(InputStream in) throws IOException {
        StringBuffer sb = new StringBuffer();
        in = new BufferedInputStream(in);
        int i = in.read();
        while (i != -1) {
            sb.append((char)i);
            i = in.read();
        }
        java.lang.String content = sb.toString();
        return content;
    }

    private static class TestValidationEventHandler implements ValidationEventHandler {
        public boolean handleEvent(ValidationEvent validationEvent) {
            System.out.println(validationEvent.getMessage());
            return true;
        }
    }
}
