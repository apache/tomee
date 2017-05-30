/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.client;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v1CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v1CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 */
public class HttpsConnectionTest {

    private final String SERVER = "localhost";
    private final int SERVER_PORT = 12345;
    private HttpsSimpleServer httpsSimpleServer;
    static final String STORE_PATH = "target/keystore";
    static final String STORE_PWD = "changeit";

    @Before
    public void init() throws IOException, NoSuchAlgorithmException, KeyManagementException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        //create key
        createKeyStore();
        //start web server
        httpsSimpleServer = new HttpsSimpleServer(SERVER_PORT, STORE_PATH, STORE_PWD);
    }

    @After
    public void close() {
        httpsSimpleServer.close();
        httpsSimpleServer = null;
        dropKeyStore();
    }

    @Test
    public void testHttps() throws URISyntaxException, IOException {
        final HttpConnectionFactory factory = new HttpConnectionFactory();
        final String url = "https://" + SERVER + ":" + SERVER_PORT + "/secure" +
                "?sslKeyStore=" + STORE_PATH + "&sslKeyStorePassword=" + STORE_PWD + "&sslKeyStoreProvider=SunX509&sslKeyStoreType=jks" +
                "&sslTrustStore=" + STORE_PATH + "&sslTrustStorePassword=" + STORE_PWD + "&readTimeout=500";
        Connection connection = factory.getConnection(new URI(url));

        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();
        String line;
        try {
            br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            connection.close();
        }

        Assert.assertTrue("should contain", sb.toString().contains("secure"));
    }

    private File createKeyStore() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        dropKeyStore();
        File keyStore = new File(STORE_PATH);

        keyStore.getParentFile().mkdirs();
        try (final FileOutputStream fos = new FileOutputStream(keyStore)) {
            final KeyPairGenerator keyGenerator = KeyPairGenerator.getInstance("RSA");
            keyGenerator.initialize(1024);

            final KeyPair pair = keyGenerator.generateKeyPair();

            final boolean addBc = Security.getProvider("BC") == null;
            if (addBc) {
                Security.addProvider(new BouncyCastleProvider());
            }
            try {

                final X509v1CertificateBuilder x509v1CertificateBuilder = new JcaX509v1CertificateBuilder(
                        new X500Name("cn=" + SERVER),
                        BigInteger.valueOf(1),
                        new Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1)),
                        new Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1)),
                        new X500Name("cn=" + SERVER),
                        pair.getPublic());

                final X509CertificateHolder certHldr = x509v1CertificateBuilder
                        .build(new JcaContentSignerBuilder("SHA1WithRSA")
                                .setProvider("BC").build(pair.getPrivate()));

                final X509Certificate cert = new JcaX509CertificateConverter().setProvider("BC").getCertificate(certHldr);

                final KeyStore ks = KeyStore.getInstance("JKS");
                ks.load(null, STORE_PWD.toCharArray());
                ks.setKeyEntry(SERVER, pair.getPrivate(), STORE_PWD.toCharArray(), new Certificate[]{cert});
                ks.store(fos, STORE_PWD.toCharArray());
            } finally {
                if (addBc) {
                    Security.removeProvider("BC");
                }
            }
        } catch (final Exception e) {
            Assert.fail(e.getMessage());
        }
        return keyStore;
    }

    private void dropKeyStore() {
        File keyStore = new File(STORE_PATH);
        if (keyStore.exists()) {
            keyStore.delete();
        }
    }


}
