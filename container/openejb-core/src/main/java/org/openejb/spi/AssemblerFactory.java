package org.openejb.spi;

import java.util.Properties;

import org.openejb.EnvProps;
import org.openejb.OpenEJBException;
import org.openejb.util.SafeToolkit;

public class AssemblerFactory {

    public static Assembler getAssembler(Properties props) throws OpenEJBException {
        SafeToolkit toolkit = SafeToolkit.getToolkit("AssemblerFactory");

        toolkit.getSafeProperties(props);

        String className = props.getProperty(EnvProps.ASSEMBLER, "org.openejb.alt.assembler.classic.Assembler");

        Assembler asse = (Assembler) toolkit.newInstance(className);
        asse.init(props);
        return asse;
    }
}
