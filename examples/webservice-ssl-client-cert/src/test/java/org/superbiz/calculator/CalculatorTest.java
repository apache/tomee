/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.superbiz.calculator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import javax.xml.namespace.QName;
import jakarta.xml.ws.BindingProvider;
import jakarta.xml.ws.Service;
import junit.framework.TestCase;
import org.apache.cxf.configuration.jsse.TLSClientParameters;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.transport.http.HTTPConduit;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class CalculatorTest {

    @Deployment(testable = false)
    public static Archive<?> app() {
        return ShrinkWrap.create(WebArchive.class, "app.war")
                .addClasses(CalculatorWs.class, CalculatorImpl.class);
    }

    @ArquillianResource
    private URL base;

    /**
     * Create a webservice client using wsdl url
     *
     * @throws Exception
     */
    //START SNIPPET: webservice
    @Test
    public void remoteCallWithSslClient() throws Exception {
        // create the service from the WSDL
        final URL url = new URL(base.toExternalForm() + "webservices/CalculatorImpl?wsdl");
        final QName calcServiceQName = new QName("http://superbiz.org/wsdl", "CalculatorWsService");
        final Service calcService = Service.create(url, calcServiceQName);

        assertNotNull(calcService);

        // get the port for the service
        final CalculatorWs calc = calcService.getPort(CalculatorWs.class);

        // switch the target URL for invocation to HTTPS
        ((BindingProvider) calc).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, "https://localhost:8443/app/webservices/CalculatorImpl");

        // add the SSL Client certificate, set the trust store and the hostname verifier
        setupTLS(calc);

        // call the remote JAX-WS webservice
        assertEquals(10, calc.sum(4, 6));
        assertEquals(12, calc.multiply(3, 4));
    }
    //END SNIPPET: webservice


    public static void setupTLS(final Object port) throws GeneralSecurityException, IOException {

        final HTTPConduit httpConduit = (HTTPConduit) ClientProxy.getClient(port).getConduit();

        final TLSClientParameters tlsCP = new TLSClientParameters();
        final String storePassword = "keystorePass";
        final String keyPassword = "clientPassword";
        final KeyStore keyStore = KeyStore.getInstance("jks");
        final String keyStoreLoc = "META-INF/clientStore.jks";
        keyStore.load(Thread.currentThread().getContextClassLoader().getResourceAsStream(keyStoreLoc), storePassword.toCharArray());

        // set the key managers from the Java KeyStore we just loaded
        final KeyManager[] myKeyManagers = getKeyManagers(keyStore, keyPassword);
        tlsCP.setKeyManagers(myKeyManagers);
        tlsCP.setCertAlias("clientalias"); // in case there is multiple certs in the keystore, make sure we pick the one we want

        // Create a trust manager that does not validate certificate chains
        // this should not be done in production. It's recommended to create a cacerts with the certificate chain or
        // to rely on a well known CA such as Verisign which is already available in the JVM
        TrustManager[] trustAllCerts = getTrustManagers();
        tlsCP.setTrustManagers(trustAllCerts);

        // don't check the host name of the certificate to match the server (running locally)
        // this should not be done on a real production system
        tlsCP.setHostnameVerifier((s, sslSession) -> true);

        httpConduit.setTlsClientParameters(tlsCP);
    }

    private static TrustManager[] getTrustManagers() throws NoSuchAlgorithmException, KeyStoreException {
        return new TrustManager[]{
                new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }
                    public void checkClientTrusted(
                            java.security.cert.X509Certificate[] certs, String authType) {
                    }
                    public void checkServerTrusted(
                            java.security.cert.X509Certificate[] certs, String authType) {
                    }
                }
        };
    }

    private static KeyManager[] getKeyManagers(KeyStore keyStore, String keyPassword) throws GeneralSecurityException, IOException {
        String alg = KeyManagerFactory.getDefaultAlgorithm();
        char[] keyPass = keyPassword != null ? keyPassword.toCharArray() : null;
        KeyManagerFactory fac = KeyManagerFactory.getInstance(alg);
        fac.init(keyStore, keyPass);
        return fac.getKeyManagers();
    }

}
