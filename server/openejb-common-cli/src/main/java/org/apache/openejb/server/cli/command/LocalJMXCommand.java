package org.apache.openejb.server.cli.command;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;
import javax.management.Attribute;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.RuntimeMBeanException;
import org.apache.openejb.monitoring.LocalMBeanServer;
import org.apache.openejb.server.cli.StreamManager;
import org.apache.xbean.propertyeditor.PropertyEditors;

@Command(name = "jmx", usage = "jmx <operation> <object-name>", description = "consult/update a jmx information")
public class LocalJMXCommand extends AbstractCommand {
    public static void main(String[] args) {
        LocalJMXCommand c = new LocalJMXCommand();
        c.setStreamManager(new StreamManager(System.out, System.err, "\n"));
        c.execute("jmx set LoggerNames java.util.logging:type=Logging foo");
    }

    @Override
    public void execute(final String cmd) {
        final String jmxCmd = cmd.substring("jmx".length()).trim();
        if ("list".equals(jmxCmd)) {
            listMBeans();
            return;
        }

        if (!jmxCmd.contains(" ")) {
            streamManager.writeErr("the command is not correct");
            return;
        }

        int space = jmxCmd.indexOf(" ");
        final String command = jmxCmd.substring(0, space);
        final String value = jmxCmd.substring(command.length()).trim();
        if ("get".equals(command)) {
            get(value);
        } else if ("set".equals(command)) {
            set(value);
        } else if ("invoke".equals(command)) {
            invoke(value);
        } else {
            streamManager.writeOut("unknow command '" + command + "'");
        }
    }

    private void invoke(final String value) {
        // TODO
        streamManager.writeOut("currently invocation are not supported");
    }

    private void get(final String cmd) {
        int space = cmd.indexOf(" ");
        if (space < 0) {
            streamManager.writeErr("you need to specify an attribute and an objectname");
            return;
        }

        final String attr = cmd.substring(0, space);
        final String on = cmd.substring(space, cmd.length()).trim();

        final MBeanServer mBeanServer = LocalMBeanServer.get();
        try {
            final ObjectName oname = new ObjectName(on);
            final Object value = mBeanServer.getAttribute(oname, attr);
            streamManager.writeOut("Attribute [" + on + " -> " + attr + "] = " + stringify(value));
        } catch (Exception ex) {
            streamManager.writeErr(ex);
        }
    }

    private void set(final String cmd) {
        final String[] split = cmd.split(" ");
        if (split.length < 2) {
            streamManager.writeErr("you need to specify an attribute, an objectname and a value");
            return;
        }

        final MBeanServer mBeanServer = LocalMBeanServer.get();
        final String newValue = cmd.substring(split[0].length() + split[1].length() + 1).trim();
        try {
            final ObjectName oname = new ObjectName(split[1]);
            final MBeanInfo minfo = mBeanServer.getMBeanInfo(oname);
            final MBeanAttributeInfo attrs[] = minfo.getAttributes();

            String type = String.class.getName();
            for (int i = 0; i < attrs.length; i++) {
                if (attrs[i].getName().equals(split[0])) {
                    type = attrs[i].getType();
                    break;
                }
            }

            final Object valueObj = PropertyEditors.getValue(type, newValue, Thread.currentThread().getContextClassLoader());
            mBeanServer.setAttribute(oname, new Attribute(split[0], valueObj));
            streamManager.writeOut("done");
        } catch (Exception ex) {
            streamManager.writeOut("Error - " + ex.toString());
        }
    }

    private String stringify(final Object value) {
        if (value == null) {
            return "<null>";
        }
        if (value.getClass().isArray()) {
            return Arrays.asList((Object[]) value).toString();
        }
        return value.toString();
    }

    private void listMBeans() {
        final MBeanServer mBeanServer = LocalMBeanServer.get();

        final Set<ObjectName> names;
        try {
            names = mBeanServer.queryNames(null, null);
        } catch (Exception e) {
            streamManager.writeErr(e);
            return;
        }

        final Iterator<ObjectName> it = names.iterator();
        while (it.hasNext()) {
            ObjectName oname = it.next();
            streamManager.writeOut("Name: " + oname.toString());

            try {
                final MBeanInfo minfo = mBeanServer.getMBeanInfo(oname);
                String code = minfo.getClassName();
                if ("org.apache.commons.modeler.BaseModelMBean".equals(code)) {
                    code = (String) mBeanServer.getAttribute(oname, "modelerType");
                }
                streamManager.writeOut("  + modelerType: " + code);

                MBeanAttributeInfo attrs[] = minfo.getAttributes();
                Object value = null;

                for (int i = 0; i < attrs.length; i++) {
                    if (!attrs[i].isReadable()) {
                        continue;
                    }

                    final String attName = attrs[i].getName();
                    if ("modelerType".equals(attName)) {
                        continue;
                    }

                    if (attName.indexOf("=") >= 0 ||
                            attName.indexOf(":") >= 0 ||
                            attName.indexOf(" ") >= 0) {
                        continue;
                    }

                    try {
                        value = mBeanServer.getAttribute(oname, attName);
                    } catch (RuntimeMBeanException uoe) {
                        // ignored
                    } catch (Throwable t) {
                        streamManager.writeErr(new Exception(t));
                        continue;
                    }

                    try {
                        String valueString = stringify(value);
                        streamManager.writeOut("  + " + attName + ": " + valueString);
                    } catch (Throwable t) {
                        streamManager.writeErr(new Exception(t));
                    }
                }
            } catch (Throwable t) {
                streamManager.writeErr(new Exception(t));
            }
            streamManager.writeOut("");
        }
    }
}
