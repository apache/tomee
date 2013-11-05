package org.apache.openejb.client;

import javax.net.ssl.*;
import java.io.FileInputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

public class SSLContextBuilder {
    private Map<String, String> params;

    public SSLContextBuilder(Map<String, String> params) {
        this.params = params;
    }

    public SSLContext build() throws NoSuchAlgorithmException, KeyManagementException {
        final KeyManager[] keyManagers = initKeyManager();
        final TrustManager[] trustManagers = initTrustManager();
        final SSLContext sslContext = SSLContext.getInstance("SSL");
        sslContext.init(keyManagers, trustManagers, new java.security.SecureRandom());
        return sslContext;
    }

    private TrustManager[] initTrustManager() {
        final String trustStore = params.get("sslTrustStore");
        if (trustStore == null) {
            return null;
        }

        try {
            String sslTrustStoreType = params.get("sslTrustStoreType");
            KeyStore ks = KeyStore.getInstance(null == sslTrustStoreType ? KeyStore.getDefaultType() : sslTrustStoreType);
            String trustStorePwd = params.get("sslTrustStorePassword");
            char[] pwd;
            if (trustStorePwd != null) {
                pwd = trustStorePwd.toCharArray();
            } else {
                pwd = "changeit".toCharArray();
            }
            FileInputStream fis = new FileInputStream(trustStore);
            try {
                ks.load(fis, pwd);
            } finally {
                fis.close();
            }
            String sslTrustStoreProvider = params.get("sslTrustStoreProvider");
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(null == sslTrustStoreProvider ? TrustManagerFactory.getDefaultAlgorithm() : sslTrustStoreProvider);
            tmf.init(ks);
            return tmf.getTrustManagers();
        } catch (final Exception e) {
            throw new ClientRuntimeException(e.getMessage(), e);
        }
    }

    private KeyManager[] initKeyManager() {
        final String keyStore = params.get("sslKeyStore");
        if (keyStore == null) {
            return null;
        }

        try {
            String sslKeyStoreType = params.get("sslKeyStoreType");
            KeyStore ks = KeyStore.getInstance(null == sslKeyStoreType ? KeyStore.getDefaultType() : sslKeyStoreType);
            String keyStorePassword = params.get("sslKeyStorePassword");
            char[] pwd;
            if (keyStorePassword != null) {
                pwd = keyStorePassword.toCharArray();
            } else {
                pwd = "changeit".toCharArray();
            }
            FileInputStream fis = new FileInputStream(keyStore);
            try {
                ks.load(fis, pwd);
            } finally {
                fis.close();
            }
            String sslKeyStoreProvider = params.get("sslKeyStoreProvider");
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(null == sslKeyStoreProvider ? KeyManagerFactory.getDefaultAlgorithm() : sslKeyStoreProvider);
            kmf.init(ks, pwd);
            return kmf.getKeyManagers();
        } catch (final Exception e) {
            throw new ClientRuntimeException(e.getMessage(), e);
        }
    }
}