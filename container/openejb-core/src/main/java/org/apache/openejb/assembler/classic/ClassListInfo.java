package org.apache.openejb.assembler.classic;

import java.util.HashSet;
import java.util.Set;

public class ClassListInfo extends InfoObject {
    public String name;
    public final Set<String> list = new HashSet<String>();
}
