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

import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import java.util.Map;

public class JndiTreeHelperImpl {

    private final Context context;
    private final String path;

    private JndiTreeHelperImpl(Context ctx, String name) {
        path = name;
        context = ctx;
    }

    private Map<String, Object> runOnTree(final Map<String, Object> root) {
        final NamingEnumeration<Binding> ne;
        try {
            ne = context.listBindings(path);
        } catch (NamingException e) {
            return root;
        }

        while (ne.hasMoreElements()) {
            final Binding current;
            try {
                current = ne.next();
            } catch (NamingException nnfe) {
                continue;
            }

            final String name = current.getName();
            final String fullName = path.concat("/").concat(name);
            final Object obj = current.getObject();

            if (obj != null && obj instanceof Context) {
                runOnJndiTree(context, JndiHelperImpl.createTreeNode(JndiHelperImpl.CONTEXT_NODE_TYPE, name, root), fullName);
            } else {
                JndiHelperImpl.createTreeNode(JndiHelperImpl.LEAF_NODE_TYPE, fullName, root);
            }
        }
        return root;
    }

    private static Map<String, Object> runOnJndiTree(final Context ctx, final Map<String, Object> root, final String prefix) {
        return (new JndiTreeHelperImpl(ctx, prefix)).runOnTree(root);
    }

    public static Map<String, Object> runOnJndiTree(final Context ctx, final Map<String, Object> root) {
        return (new JndiTreeHelperImpl(ctx, "")).runOnTree(root);
    }
}
