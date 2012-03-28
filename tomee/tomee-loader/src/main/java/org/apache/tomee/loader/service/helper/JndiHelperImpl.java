/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.tomee.loader.service.helper;

import org.apache.openejb.AppContext;
import org.apache.openejb.BeanContext;
import org.apache.openejb.ModuleContext;
import org.apache.tomee.loader.service.ServiceContext;

import javax.naming.Context;
import javax.naming.NamingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JndiHelperImpl implements JndiHelper {

    public static final String CONTEXT_NODE_TYPE = "context";
    public static final String LEAF_NODE_TYPE = "leaf";
    public static final String APPLICATION_NODE_TYPE = "application";
    public static final String MODULE_NODE_TYPE = "module";
    public static final String ROOT_NODE_TYPE = "root";

    private final ServiceContext srvCtx;

    public JndiHelperImpl(ServiceContext srvCtx) {
        this.srvCtx = srvCtx;
    }

    @Override
    public Map<String, Object> getJndi() {
        final Map<String, Object> root = JndiHelperImpl.createTreeNode(ROOT_NODE_TYPE, "/", null);

        for (AppContext appContext : this.srvCtx.getOpenEJBHelper().getAppContexts()) {
            final Map<String, Object> appNode = JndiHelperImpl.createTreeNode(APPLICATION_NODE_TYPE, appContext.getId(), root);

            // is there a simpler way?
            // id = guarantee unity
            final Map<String, ModuleContext> modules = new HashMap<String, ModuleContext>();
            for (BeanContext beanContext : appContext.getBeanContexts()) {
                if (!beanContext.getBeanClass().equals(BeanContext.Comp.class)) {
                    final ModuleContext moduleContext = beanContext.getModuleContext();
                    modules.put(moduleContext.getUniqueId(), moduleContext);
                }
            }

            for (ModuleContext module : modules.values()) {
                final Map<String, Object> moduleNode = JndiHelperImpl.createTreeNode(MODULE_NODE_TYPE, appContext.getId(), appNode);
                addSubContext(module.getModuleJndiContext(), "module", moduleNode);
            }

            addSubContext(appContext.getAppJndiContext(), "app", appNode);
            addSubContext(appContext.getGlobalJndiContext(), "global", appNode);
        }

        return root;
    }

    private void addSubContext(final Context context, final String subContext, final Map<String, Object> parent) {
        final Map<String, Object> subNode = JndiHelperImpl.createTreeNode(CONTEXT_NODE_TYPE, subContext, parent);

        try {
            JndiTreeHelperImpl.runOnJndiTree((Context) context.lookup(subContext), subNode);
        } catch (NamingException e) {
            //do nothing
        }
    }

    public static Map<String, Object> createTreeNode(String type, String path, Map<String, Object> parent) {
        final Map<String, Object> result = new HashMap<String, Object>();
        result.put("type", type);
        result.put("path", path);
        
        if(parent != null) {
            List<Map<String, Object>> children = (List<Map<String, Object>>) parent.get("children");
            if(children == null) {
                children = new ArrayList<Map<String, Object>>();
                parent.put("children", children);
            }
            children.add(result);
        }

        return result;
    }
}
