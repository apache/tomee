package org.apache.openejb.server.httpd;

import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @author Romain Manni-Bucau
 */
public class ServletByteArrayIntputStream extends ServletInputStream {
    private ByteArrayInputStream intputStream;

    public ServletByteArrayIntputStream(byte[] body) {
        intputStream = new ByteArrayInputStream(body);
    }

    @Override
    public int read() throws IOException {
        return intputStream.read();
    }

    public ByteArrayInputStream getIntputStream() {
        return intputStream;
    }
}
