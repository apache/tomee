package org.apache.openejb.log;

import java.util.logging.ConsoleHandler;
import org.apache.openejb.loader.SystemInstance;
import org.fusesource.jansi.AnsiConsole;
import org.fusesource.jansi.AnsiOutputStream;
import org.fusesource.jansi.WindowsAnsiOutputStream;

import static org.apache.openejb.log.JULUtil.level;

public class ConsoleColorHandler extends ConsoleHandler {
    private static boolean wrapped;

    static {
        // mess output with maven on linux and not really mandatory in linux
        // TODO: not tested under windows, it needs to add jna
        wrapped = AnsiConsole.wrapOutputStream(System.out) instanceof WindowsAnsiOutputStream;
        if (wrapped && "true".equals(SystemInstance.get().getProperty("openejb.log.color.install", "true"))) {
            AnsiConsole.systemInstall();
        }
    }

    public ConsoleColorHandler() {
        setFormatter(new ColorFormatter());
        setLevel(level());
        if (wrapped) {
            setOutputStream(AnsiConsole.out);
        } else {
            setOutputStream(System.out);
        }
    }
}
