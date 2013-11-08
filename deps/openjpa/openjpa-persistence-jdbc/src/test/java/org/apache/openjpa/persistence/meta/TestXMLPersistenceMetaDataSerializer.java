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
package org.apache.openjpa.persistence.meta;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;


import org.apache.openjpa.persistence.meta.common.apps.MetaTest1;
import org.apache.openjpa.persistence.meta.common.apps.MetaTest2;
import org.apache.openjpa.persistence.meta.common.apps.MetaTest3;
import org.apache.openjpa.persistence.meta.common.apps.MetaTest5;
import org.apache.openjpa.persistence.meta.common.apps.MetaTest6;
import org.apache.openjpa.conf.OpenJPAConfiguration;
import org.apache.openjpa.conf.OpenJPAConfigurationImpl;
import org.apache.openjpa.meta.MetaDataRepository;
import org.apache.openjpa.persistence.XMLPersistenceMetaDataParser;
import org.apache.openjpa.persistence.XMLPersistenceMetaDataSerializer;

/**
 * <p>Tests the {@link MetaDataSerializer} by parsing all the metadata
 * files, serializing them to a buffer, then deserializing them from the
 * buffer and invoking the tests defined by {@link TestClassMetaData}.</p>
 *
 * @author Abe White
 */
public class TestXMLPersistenceMetaDataSerializer
    extends TestClassMetaData {

    public TestXMLPersistenceMetaDataSerializer(String test) {
        super(test);
    }

    protected MetaDataRepository getRepository()
        throws Exception {
        OpenJPAConfiguration conf = new OpenJPAConfigurationImpl();
        MetaDataRepository repos = conf.newMetaDataRepositoryInstance();
        repos.getMetaData(MetaTest5.class, null, true);
        repos.getMetaData(MetaTest3.class, null, true);
        repos.getMetaData(MetaTest2.class, null, true);
        repos.getMetaData(MetaTest1.class, null, true);
        repos.getMetaData(MetaTest6.class, null, true);

        XMLPersistenceMetaDataSerializer ser =
            new XMLPersistenceMetaDataSerializer(conf);
        ser.addAll(repos);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ser.serialize(new OutputStreamWriter(out), ser.PRETTY);
        byte[] bytes = out.toByteArray();

        XMLPersistenceMetaDataParser parser =
            new XMLPersistenceMetaDataParser(conf);
        parser.parse(new InputStreamReader
            (new ByteArrayInputStream(bytes)), "bytes");
        return parser.getRepository();
    }
}
