package org.apache.openejb.client;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

/**
 */
public class HttpsConnectionTest {

    private final String SERVER = "localhost";
    private final int SERVER_PORT = 12345;
    private HttpsSimpleServer httpsSimpleServer;
    static final String STORE_PATH="target/keystore";
    static final String STORE_PWD="changeit";

    @Before
    public void init() throws IOException, NoSuchAlgorithmException, KeyManagementException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        //create key
        createKeyStore();
        //start web server
        httpsSimpleServer = new HttpsSimpleServer(SERVER_PORT,STORE_PATH, STORE_PWD);
    }

    @After
    public void close(){
        httpsSimpleServer = null;
        dropKeyStore();
    }

    @Test
    public void testHttps() throws URISyntaxException, IOException {
        String url = "https://"+SERVER+":" + SERVER_PORT +"/secure"+
                "?sslKeyStore=" +STORE_PATH+"&sslKeyStorePassword=" +STORE_PWD+"&sslKeyStoreProvider=SunX509&sslKeyStoreType=jks"+
                "&sslTrustStore="+STORE_PATH+"&sslTrustStorePassword="+STORE_PWD+"&readTimeout=500";
        Connection connection = new HttpConnectionFactory().getConnection(new URI(url));

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
        }

        Assert.assertTrue("should contain",sb.toString().contains("secure"));
    }

    private File createKeyStore() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        dropKeyStore();
        File keyStore = new File(STORE_PATH);

        Class<?> keyToolClass;
        try {
            keyToolClass = Class.forName("sun.security.tools.KeyTool");
        } catch (final ClassNotFoundException e) {
            keyToolClass = Class.forName("com.ibm.crypto.tools.KeyTool");
        }

        final String[] args = {
                "-genkey",
                "-alias", SERVER,
                "-keypass", STORE_PWD,
                "-keystore", keyStore.getAbsolutePath(),
                "-storepass", STORE_PWD,
                "-dname", "cn="+SERVER,
                "-keyalg", "RSA"
        };
        keyToolClass.getMethod("main", String[].class).invoke(null, new Object[]{args});

        return keyStore;
    }

    private void dropKeyStore() {
        File keyStore = new File(STORE_PATH);
        if (keyStore.exists()){
            keyStore.delete();
        }
    }


}
