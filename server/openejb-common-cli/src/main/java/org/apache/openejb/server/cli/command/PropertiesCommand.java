package org.apache.openejb.server.cli.command;

import org.apache.openejb.assembler.classic.OpenEjbConfiguration;
import org.apache.openejb.assembler.classic.cmd.Info2Properties;
import org.apache.openejb.loader.SystemInstance;

import java.io.PrintStream;

public class PropertiesCommand extends AbstractCommand {
    @Override
    public String name() {
        return "properties";
    }

    @Override
    public String description() {
        return "show openejb container properties";
    }

    @Override
    public void execute(final String cmd) {
        final OpenEjbConfiguration config = SystemInstance.get().getComponent(OpenEjbConfiguration.class);
        Info2Properties.printConfig(config, new PrintStream(streamManager.getOut()), streamManager.getLineSep());
    }
}
