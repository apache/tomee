package org.apache.openejb.server.httpd;

import javax.servlet.ServletOutputStream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @author Romain Manni-Bucau
 */
public class ServletByteArrayOutputStream extends ServletOutputStream {
    private ByteArrayOutputStream outputStream;

    public ServletByteArrayOutputStream() {
        outputStream = new ByteArrayOutputStream();
    }

    @Override
    public void write(int b) throws IOException {
        outputStream.write(b);
    }

    public ByteArrayOutputStream getOutputStream() {
        return outputStream;
    }
}
