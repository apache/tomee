package org.apache.openejb.assembler.classic;

import java.util.List;
import java.util.ArrayList;

public class StatefulBeanInfo extends EnterpriseBeanInfo {

    public StatefulBeanInfo() {
        type = STATEFUL;
    }

    public List<LifecycleCallbackInfo> postActivate = new ArrayList();
    public List<LifecycleCallbackInfo> prePassivate = new ArrayList();

}
