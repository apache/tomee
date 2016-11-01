package org.apache.openejb.client;

import static javax.xml.bind.DatatypeConverter.printBase64Binary;

import java.io.Serializable;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class JNDIContextAuth implements Serializable {
    private static final long serialVersionUID = 1L;
    public final String username;
    public final char[] password;

    public JNDIContextAuth(String username, String password) {
        this.username = username;
        if (password != null) {
            this.password = password.toCharArray();
        } else {
            this.password = new char[0];
        }
        checkConstraints();
    }

    public void checkConstraints() {
        if (username == null) {
            throw new IllegalArgumentException("username cannot be null, don't use this class if you don't have a username");
        }
    }

    public void setAuthenticationHeader(HttpURLConnection httpURLConnection) {
        httpURLConnection.setRequestProperty("Authorization", "Basic " + toEncodedString());
    }

    public String toEncodedString() {
        byte[] message = (username + ":" + String.valueOf(password)).getBytes(StandardCharsets.UTF_8);
        String encoded = printBase64Binary(message);
        return encoded;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(password);
        result = prime * result + ((username == null) ? 0 : username.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof JNDIContextAuth)) {
            return false;
        }
        JNDIContextAuth other = (JNDIContextAuth) obj;
        if (!Arrays.equals(password, other.password)) {
            return false;
        }
        if (username == null) {
            if (other.username != null) {
                return false;
            }
        } else if (!username.equals(other.username)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "JNDIContextAuth [username=" + username + ", password=" + Arrays.toString(password) + "]";
    }
}
