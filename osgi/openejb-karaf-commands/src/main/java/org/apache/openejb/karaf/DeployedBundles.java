package org.apache.openejb.karaf;

import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.apache.openejb.core.osgi.impl.Deployer;
import org.apache.openejb.karaf.table.Line;
import org.apache.openejb.karaf.table.Lines;
import org.osgi.framework.Bundle;

@Command(scope = "openejb", name = "bundles", description = "Lists all deployed bundles")
public class DeployedBundles extends OsgiCommandSupport {
    @Override
    protected Object doExecute() throws Exception {
        Lines lines = new Lines();
        lines.add(new Line("Id", "Symbolic name", "Version"));
        for (Bundle bundle : Deployer.instance().deployedBundles()) {
            lines.add(new Line(Long.toString(bundle.getBundleId()), bundle.getSymbolicName(), bundle.getVersion().toString()));
        }

        lines.print(System.out);
        return null;
    }
}
