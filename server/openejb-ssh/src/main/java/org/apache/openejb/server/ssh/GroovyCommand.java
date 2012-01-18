package org.apache.openejb.server.ssh;

import jline.ConsoleReader;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.openejb.server.groovy.OpenEJBGroovyShell;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Collection;

public class GroovyCommand implements Command, Runnable {
    public static final String EXIT_COMMAND = "exit";
    public static final String LINE_SEP = System.getProperty("line.separator");
    public static final String PROMPT = "openejb> ";

    private OpenEJBGroovyShell shell;
    private OutputStreamWriter serr;
    private OutputStreamWriter sout;
    private InputStream sin;
    private ExitCallback cbk;

    @Override
    public void setInputStream(InputStream in) {
        sin = in;
    }

    @Override
    public void setOutputStream(OutputStream out) {
        sout = new OutputStreamWriter(out);
    }

    @Override
    public void setErrorStream(OutputStream err) {
        serr = new OutputStreamWriter(err);
    }

    @Override
    public void setExitCallback(ExitCallback callback) {
        cbk = callback;
    }

    @Override
    public void start(Environment env) throws IOException {
        shell = new OpenEJBGroovyShell();
        new Thread(this, "OpenEJB Groovy Shell " + System.identityHashCode(this)).start();
    }

    @Override
    public void destroy() {
        shell.resetLoadedClasses();
    }

    @Override
    public void run() {
        try {
            final ConsoleReader reader = new ConsoleReader(sin, sout);
            reader.setBellEnabled(false);
            // TODO : add completers with method names...?

            // TODO: why is it adding spaces?
            String line;
            while ((line = reader.readLine(PROMPT)) != null) {
                if (EXIT_COMMAND.equals(line)) {
                    break;
                }

                try {
                    write(sout, result(line));
                } catch (SshRuntimeException sshEx) {
                    write((Exception) sshEx.getCause());
                }
            }
        } catch (IOException e) {
            throw new SshRuntimeException(e);
        } finally {
            cbk.onExit(0);
        }
    }

    private static void write(final OutputStreamWriter writer, final String s) {
        for (String l : s.split(LINE_SEP)) {
            try {
                writer.write(l);
                writer.write(LINE_SEP);
                writer.flush();
            } catch (IOException e) {
                // ignored
            }
        }
    }

    private void write(Exception e) {
        if (e.getStackTrace() == null) {
            write(serr, e.getMessage());
        } else {
            final StringBuilder error = new StringBuilder();
            for (StackTraceElement elt : e.getStackTrace()) {
                error.append(elt.toString() + LINE_SEP);
            }
            write(serr, error.toString());
        }
    }

    private String result(final String value) {
        Object out;
        try {
            out = shell.evaluate(value);
        } catch (Exception e) {
            throw new SshRuntimeException(e);
        }

        if (out == null) {
            return "null";
        }
        if (out instanceof Collection) {
            final StringBuilder builder = new StringBuilder();
            for (Object o : (Collection) out) {
                builder.append(string(o) + LINE_SEP);
            }
        }
        if (out != null) {
            return string(out);
        }
        return null;
    }

    private static String string(Object out) {
        if (!out.getClass().getName().startsWith("java")) {
            return ToStringBuilder.reflectionToString(out, ToStringStyle.SHORT_PREFIX_STYLE);
        }
        return out.toString();
    }
}
