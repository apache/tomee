package org.apache.openejb.server.httpd;

import javax.servlet.ServletInputStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Romain Manni-Bucau
 */
public class ServletIntputStreamAdapter extends ServletInputStream {
    private InputStream intputStream;

    public ServletIntputStreamAdapter(InputStream is) {
        intputStream = is;
    }

    @Override
    public int read() throws IOException {
        return intputStream.read();
    }
}
