package org.apache.openejb.server.httpd;

import javax.servlet.ServletOutputStream;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author Romain Manni-Bucau
 */
public class ServletOutputStreamAdapter extends ServletOutputStream {
    private OutputStream outputStream;

    public ServletOutputStreamAdapter(OutputStream os) {
        outputStream = os;
    }

    @Override
    public void write(int b) throws IOException {
        outputStream.write(b);
    }
}
