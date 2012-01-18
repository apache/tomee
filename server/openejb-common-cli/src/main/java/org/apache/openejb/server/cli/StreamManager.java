/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.server.cli;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Collection;

public class StreamManager {
    private String lineSep;
    private OutputStreamWriter serr;
    private OutputStreamWriter sout;
    private OutputStream out;
    private OutputStream err;

    public StreamManager(OutputStream out, OutputStream err, String lineSep) {
        this.lineSep = lineSep;
        this.out = out;
        this.err = err;
        this.sout = new OutputStreamWriter(out);
        this.serr= new OutputStreamWriter(err);
    }

    private void write(final OutputStreamWriter writer, final String s) {
        for (String l : s.split(lineSep)) {
            try {
                writer.write(l);
                writer.write(lineSep);
                writer.flush();
            } catch (IOException e) {
                // ignored
            }
        }
    }

    public void writeOut(final String s) {
        write(sout, s);
    }

    public void writeErr(final Exception e) {
        if (e.getStackTrace() == null) {
            write(serr, e.getMessage());
        } else {
            final StringBuilder error = new StringBuilder();
            for (StackTraceElement elt : e.getStackTrace()) {
                error.append(elt.toString()).append(lineSep);
            }
            write(serr, error.toString());
        }
    }

    public String asString(final Object out) {
        if (out == null) {
            return "null";
        }
        if (out instanceof Collection) {
            final StringBuilder builder = new StringBuilder();
            for (Object o : (Collection) out) {
                builder.append(string(o)).append(lineSep);
            }
            return builder.toString();
        }
        return string(out);
    }

    private static String string(final Object out) {
        if (!out.getClass().getName().startsWith("java")) {
            return ToStringBuilder.reflectionToString(out, ToStringStyle.SHORT_PREFIX_STYLE);
        }
        return out.toString();
    }

    public OutputStream getOut() {
        return out;
    }

    public OutputStream getErr() {
        return err;
    }

    public OutputStreamWriter getSerr() {
        return serr;
    }

    public OutputStreamWriter getSout() {
        return sout;
    }

    public String getLineSep() {
        return lineSep;
    }

    public void writeErr(final String s) {
        write(serr, s);
    }
}
