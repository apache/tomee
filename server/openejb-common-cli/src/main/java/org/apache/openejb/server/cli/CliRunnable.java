package org.apache.openejb.server.cli;

import jline.ConsoleReader;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.openejb.assembler.classic.OpenEjbConfiguration;
import org.apache.openejb.assembler.classic.cmd.Info2Properties;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.server.groovy.OpenEJBGroovyShell;
import org.apache.openejb.util.helper.CommandHelper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.util.Collection;

public class CliRunnable implements Runnable {
    public static final String EXIT_COMMAND = "exit";
    private static final String GROOVY_PREFIX = "G ";
    private static final String LIST_CMD = "list";
    private static final String PROPERTIES_CMD = "properties";
    private static final String WELCOME = "Welcome on your $bind:$port $name server";
    public static final String OS_LINE_SEP = System.getProperty("line.separator");
    private static final String NAME;
    public static final String PROMPT = "openejb> ";

    static {
        String name = "OpenEJB";
        try {
            CliRunnable.class.getClassLoader().loadClass("org.apache.tomee.loader.TomcatHook");
            name = "TomEE";
        } catch (ClassNotFoundException cnfe) {
            // ignored, we are using a simple OpenEJB server
        }
        NAME = name;
    }

    public String lineSep;

    private OpenEJBGroovyShell shell;
    private OutputStreamWriter serr;
    private OutputStreamWriter sout;
    private OutputStream out;
    private InputStream sin;
    private String bind;
    private int port;


    public CliRunnable(String bind, int port) {
        this(bind, port, null);
    }

    public CliRunnable(String bind, int port, String sep) {
        this.bind = bind;
        this.port = port;

        if (sep != null) { // workaround to force ConsoleReader to use another line.separator
            lineSep = sep;
            System.setProperty("line.separator", sep);
            try {
                // just to force the loading of this class with the set line.separator
                // because ConsoleReader.CR is a constant and we need sometimes another value
                // not a big issue but keeping this as a workaround
                new ConsoleReader();
            } catch (IOException ignored) {
                // no-op
            } finally {
                System.setProperty("line.separator", OS_LINE_SEP);
            }
        } else {
            lineSep = OS_LINE_SEP;
        }
    }

    public void setInputStream(InputStream in) {
        sin = in;
    }

    public void setOutputStream(OutputStream out) {
        this.out = out;
        sout = new OutputStreamWriter(out);
    }

    public void setErrorStream(OutputStream err) {
        serr = new OutputStreamWriter(err);
    }

    public void start() throws IOException {
        shell = new OpenEJBGroovyShell();
        new Thread(this, "OpenEJB Cli").start();
    }

    public void destroy() {
        shell.resetLoadedClasses();
    }

    public void run() {
        try {
            final ConsoleReader reader = new ConsoleReader(sin, sout);
            // TODO : add completers with method names...?

            String line;
            write(sout, WELCOME // simple replace for now, if it is mandatory we could bring velocity to do it
                    .replace("$bind", bind)
                    .replace("$port", Integer.toString(port))
                    .replace("$name", NAME));
            while ((line = reader.readLine(PROMPT)) != null) {
                if (EXIT_COMMAND.equals(line)) {
                    break;
                }

                if (line.startsWith(GROOVY_PREFIX)) {
                    try {
                        write(sout, result(line.substring(GROOVY_PREFIX.length())));
                    } catch (CliRuntimeException sshEx) {
                        write((Exception) sshEx.getCause());
                    }
                } else if (LIST_CMD.equals(line)) {
                    list();
                } else if (PROPERTIES_CMD.equals(line)) {
                    properties();
                } else {
                    write(sout, "sorry i don't understand '" + line + "'");
                }
            }
        } catch (IOException e) {
            throw new CliRuntimeException(e);
        }
    }

    private void properties() {
        final OpenEjbConfiguration config = SystemInstance.get().getComponent(OpenEjbConfiguration.class);
        Info2Properties.printConfig(config, new PrintStream(out), lineSep);
    }

    private void list() {
        try {
            CommandHelper.listEJBs(lineSep).print(new PrintStream(out));
        } catch (Exception e) {
            write(e);
        }
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

    private void write(Exception e) {
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

    private String result(final String value) {
        Object out;
        try {
            out = shell.evaluate(value);
        } catch (Exception e) {
            throw new CliRuntimeException(e);
        }

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

    private static String string(Object out) {
        if (!out.getClass().getName().startsWith("java")) {
            return ToStringBuilder.reflectionToString(out, ToStringStyle.SHORT_PREFIX_STYLE);
        }
        return out.toString();
    }

    public OutputStream getOut() {
        return out;
    }

    public OutputStreamWriter getSerr() {
        return serr;
    }

    public OutputStreamWriter getSout() {
        return sout;
    }

    public InputStream getSin() {
        return sin;
    }

    public String getBind() {
        return bind;
    }

    public int getPort() {
        return port;
    }
}
