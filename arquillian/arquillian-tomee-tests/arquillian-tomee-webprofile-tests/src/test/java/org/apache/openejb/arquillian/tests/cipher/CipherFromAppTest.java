/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.arquillian.tests.cipher;

import org.apache.openejb.cipher.PasswordCipher;
import org.apache.openejb.resource.jdbc.managed.local.ManagedDataSource;
import org.apache.tomee.jdbc.TomEEDataSourceCreator;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.annotation.Resource;
import javax.sql.DataSource;

import static org.junit.Assert.assertEquals;

@RunWith(Arquillian.class)
public class CipherFromAppTest {
    @Deployment
    public static Archive<?> war() {
        return ShrinkWrap.create(WebArchive.class, CipherFromAppTest.class.getName() + ".war")
                .addClass(MyConstantCipher.class)
                .addAsWebInfResource(new StringAsset(MyConstantCipher.class.getName()), "classes/META-INF/" + PasswordCipher.class.getName() + "/constant")
                .addAsWebInfResource(new StringAsset("" +
                        "<resource>" +
                        "   <Resource type=\"DataSource\" id=\"cipher\">" +
                        "       PasswordCipher = constant\n" +
                        // tomcat-jdbc doesnt support invalid credentials
                        "       InitialSize = 0\n" +
                        "       MinIdle = 0\n" +
                        "   </Resource>" +
                        "</resource>"), "resources.xml");
    }

    @Resource(name = "cipher")
    private DataSource ds;

    @Test
    public void checkPassword() {
        final DataSource delegate = DataSource.class.cast(ManagedDataSource.class.cast(ds).getDelegate());
        assertEquals(new MyConstantCipher().decrypt(null), TomEEDataSourceCreator.TomEEDataSource.class.cast(delegate).getPoolProperties().getPassword());
    }
}
