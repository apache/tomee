package org.apache.openejb.spi;

import java.util.Properties;

import org.apache.openejb.EnvProps;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.util.SafeToolkit;

public class AssemblerFactory {

    public static Assembler getAssembler(Properties props) throws OpenEJBException {
        SafeToolkit toolkit = SafeToolkit.getToolkit("AssemblerFactory");

        toolkit.getSafeProperties(props);

        String className = props.getProperty(EnvProps.ASSEMBLER, "org.apache.openejb.assembler.classic.Assembler");

        Assembler asse = (Assembler) toolkit.newInstance(className);
        asse.init(props);
        return asse;
    }
}
