package org.apache.openejb.karaf.command;

import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.apache.openejb.assembler.classic.OpenEjbConfiguration;
import org.apache.openejb.assembler.classic.cmd.Info2Properties;
import org.apache.openejb.loader.SystemInstance;

@Command(scope = "openejb", name = "properties", description = "dump OpenEJB configuration")
public class Properties extends OsgiCommandSupport {
    @Override
    protected Object doExecute() throws Exception {
        final OpenEjbConfiguration config = SystemInstance.get().getComponent(OpenEjbConfiguration.class);
        Info2Properties.printConfig(config);
        return null;
    }
}
