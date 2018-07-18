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
package org.apache.tomee.embedded;

import org.apache.catalina.connector.Connector;
import org.apache.openejb.loader.Files;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v1CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v1CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.junit.Assert;
import org.junit.Test;

import javax.management.ObjectName;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.management.ManagementFactory;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

public class SslTomEETest {
    @Test
    public void test() throws Exception {
        final File keystore = new File("target/keystore");

        {   // generate keystore/trustore
            if (keystore.exists()) {
                Files.delete(keystore);
            }

            keystore.getParentFile().mkdirs();
            try (final FileOutputStream fos = new FileOutputStream(keystore)) {
                final KeyPairGenerator keyGenerator = KeyPairGenerator.getInstance("RSA");
                keyGenerator.initialize(1024);

                final KeyPair pair = keyGenerator.generateKeyPair();

                final boolean addBc = Security.getProvider("BC") == null;
                if (addBc) {
                    Security.addProvider(new BouncyCastleProvider());
                }
                try {

                    final X509v1CertificateBuilder x509v1CertificateBuilder = new JcaX509v1CertificateBuilder(
                            new X500Name("cn=serveralias"),
                            BigInteger.valueOf(1),
                            new Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1)),
                            new Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1)),
                            new X500Name("cn=serveralias"),
                            pair.getPublic());

                    final X509CertificateHolder certHldr = x509v1CertificateBuilder
                            .build(new JcaContentSignerBuilder("SHA1WithRSA")
                                    .setProvider("BC").build(pair.getPrivate()));

                    final X509Certificate cert = new JcaX509CertificateConverter().setProvider("BC").getCertificate(certHldr);

                    final KeyStore ks = KeyStore.getInstance("JKS");
                    ks.load(null, "changeit".toCharArray());
                    ks.setKeyEntry("serveralias", pair.getPrivate(), "changeit".toCharArray(), new Certificate[]{cert});
                    ks.store(fos, "changeit".toCharArray());
                } finally {
                    if (addBc) {
                        Security.removeProvider("BC");
                    }
                }
            } catch (final Exception e) {
                Assert.fail(e.getMessage());
            }
        }

        final Configuration configuration = new Configuration();
        configuration.setSsl(true);
        configuration.setKeystoreFile(keystore.getAbsolutePath());
        configuration.setKeystorePass("changeit");
        configuration.setKeyAlias("serveralias");

        final Container container = new Container();
        container.setup(configuration);
        container.start();
        Connector[] connectors = container.getTomcat().getService().findConnectors();
        for(Connector conn : connectors) {
        	if(conn.getPort() == 8443) {
        		Object propertyObject = conn.getProperty("keystoreFile");
                assertNotNull(propertyObject);
                assertEquals(keystore.getAbsolutePath(), propertyObject.toString());
        	}
        }

        try {
            assertEquals(8443, ManagementFactory.getPlatformMBeanServer().getAttribute(new ObjectName("Tomcat:type=ProtocolHandler,port=8443"), "port"));
        } finally {
            container.stop();
        }

        // ensure it is not always started
        configuration.setSsl(false);
        container.setup(configuration);
        container.start();
        try {
            assertFalse(ManagementFactory.getPlatformMBeanServer().isRegistered(new ObjectName("Tomcat:type=ProtocolHandler,port=8443")));
        } finally {
            container.close();
        }

    }
}
