package org.apache.openejb.server.ssh;

import jline.ConsoleReader;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.openejb.server.groovy.OpenEJBGroovyShell;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Collection;

public class OpenEJBCommands implements Command, Runnable {
    public static final String EXIT_COMMAND = "exit";
    private static final String GROOVY_PREFIX = "G ";
    private static final String WELCOME = "Welcome on your $bind:$port $name server";
    public static final String LINE_SEP = "\r\n"; // don't use line.separator (sshd use this one)
    public static final String OS_LINE_SEP = System.getProperty("line.separator");
    public static final String PROMPT = "openejb> ";

    static {
        System.setProperty("line.separator", LINE_SEP);
        try {
            // just to force the loading of this class with the set line.separator
            // because ConsoleReader.CR is a constant and we need sometimes another value
            // not a big issue but keeping this as a workaround
            final ConsoleReader reader = new ConsoleReader();
        } catch (IOException ignored) {
            // no-op
        }
        System.setProperty("line.separator", OS_LINE_SEP);
    }

    private OpenEJBGroovyShell shell;
    private OutputStreamWriter serr;
    private OutputStreamWriter sout;
    private ExitCallback cbk;
    private InputStream sin;
    private String bind;
    private int port;

    public OpenEJBCommands(String bind, int port) {
        this.bind = bind;
        this.port = port;
    }

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
            // TODO : add completers with method names...?

            String name = "OpenEJB";
            try {
                getClass().getClassLoader().loadClass("org.apache.tomee.loader.TomcatHook");
                name = "TomEE";
            } catch (ClassNotFoundException cnfe) {
                // ignored, we are using a simple OpenEJB server
            }

            String line;
            write(sout, WELCOME // simple replace for now, if it is mandatory we could bring velocity to do it
                    .replace("$bind", bind)
                    .replace("$port", Integer.toString(port))
                    .replace("$name", name));
            while ((line = reader.readLine(PROMPT)) != null) {
                if (EXIT_COMMAND.equals(line)) {
                    break;
                }

                if (line.startsWith(GROOVY_PREFIX)) {
                    try {
                        write(sout, result(line));
                    } catch (SshRuntimeException sshEx) {
                        write((Exception) sshEx.getCause());
                    }
                } else {
                    write(sout, "sorry i don't understand '" + line + "'");
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
                error.append(elt.toString()).append(LINE_SEP);
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
                builder.append(string(o)).append(LINE_SEP);
            }
            return builder.toString();
        }
        return string(out);
    }

    private static String string(Object out) {
        if (!out.getClass().getName().startsWith("java")) {
            return ToStringBuilder.reflectionToString(out, ToStringStyle.SHORT_PREFIX_STYLE);
        }
        return out.toString();
    }
}
