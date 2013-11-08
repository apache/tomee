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
package org.apache.openjpa.jdbc.meta.strats;

import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.apache.openjpa.jdbc.identifier.DBIdentifier;
import org.apache.openjpa.jdbc.kernel.JDBCStore;
import org.apache.openjpa.jdbc.meta.ValueMapping;
import org.apache.openjpa.jdbc.schema.Column;
import org.apache.openjpa.jdbc.schema.ColumnIO;
import org.apache.openjpa.jdbc.sql.DBDictionary;
import org.apache.openjpa.meta.JavaTypes;
import org.apache.openjpa.util.InternalException;

/**
 * Base class for xml value handlers.
 *
 * @author Catalina Wei
 * @since 1.0
 */
public class XMLValueHandler
    extends AbstractValueHandler {
    private static final String PROXY_SUFFIX = "$proxy";

    /**
     * @deprecated
     */
    public Column[] map(ValueMapping vm, String name, ColumnIO io,
        boolean adapt) {
        DBDictionary dict = vm.getMappingRepository().getDBDictionary();
        DBIdentifier colName = DBIdentifier.newColumn(name, dict != null ? dict.delimitAll() : false);
        return map(vm, colName, io, adapt);
    }

    public Column[] map(ValueMapping vm, DBIdentifier name, ColumnIO io,
        boolean adapt) {
        Column col = new Column();
        col.setIdentifier(name);
        col.setJavaType(JavaTypes.STRING);
        col.setSize(-1);
        col.setTypeIdentifier(DBIdentifier.newColumnDefinition(vm.getMappingRepository().getDBDictionary()
            .xmlTypeName));
        col.setXML(true);
        return new Column[]{ col };
    }

    public Object toDataStoreValue(ValueMapping vm, Object val,
        JDBCStore store) {
        // check for null value.
        if (val == null) 
            return null;
        try {
            JAXBContext jc = JAXBContext.newInstance(
                // on update val is a proxy, that can not be marshalled.
                // so we get original type if val is a proxy.
                (val.getClass().getName().endsWith(PROXY_SUFFIX))
                     ? val.getClass().getSuperclass()
                     : val.getClass());
            Marshaller m = jc.createMarshaller();
            // Some DBs, like MS SQL Server, require a different encoding than the JAXB default of UTF-8
            m.setProperty("jaxb.encoding", store.getDBDictionary().getXMLTypeEncoding());
            Writer result = new StringWriter();
            m.marshal( val, result );
            return result.toString();
        }
        catch (JAXBException je) {
            throw new InternalException(je);
        }
    }

    public Object toObjectValue(ValueMapping vm, Object val) {
        // check for null value.
        if (val == null) 
            return null;
        try {
            String className  = vm.getDeclaredType().getName();
            int i = className.lastIndexOf('.');
            String packageName = className;
            if (i != -1) {
                packageName = className.substring(0, i);
            }
            JAXBContext jc = JAXBContext.newInstance(packageName);
            Unmarshaller u = jc.createUnmarshaller();
            return u.unmarshal(new StreamSource(new StringReader
                    (val.toString())));
        }
        catch (JAXBException je) {
            throw new InternalException(je);
        }
    }
}
