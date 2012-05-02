package org.apache.openejb.util;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class LengthInputStream extends FilterInputStream {
    private long length;

    public LengthInputStream(InputStream in) throws IOException {
        super(in);
    }

    @Override
    public int read() throws IOException {
        final int i = super.read();
        if (i > 0) length++;
        return i;
    }

    @Override
    public int read(byte[] b) throws IOException {
        final int i = super.read(b);
        if (i > 0) length += i;
        return i;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        final int i = super.read(b, off, len);
        if (i > 0) length += i;
        return i;
    }

    public long getLength() {
        return length;
    }
}
