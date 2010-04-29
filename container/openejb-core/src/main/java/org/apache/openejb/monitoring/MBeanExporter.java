/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.monitoring;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanException;
import javax.management.modelmbean.ModelMBeanInfo;
import javax.management.modelmbean.RequiredModelMBean;
import javax.management.modelmbean.ModelMBeanInfoSupport;
import javax.management.modelmbean.ModelMBeanAttributeInfo;
import javax.management.modelmbean.ModelMBeanConstructorInfo;
import javax.management.modelmbean.ModelMBeanOperationInfo;
import javax.management.modelmbean.ModelMBeanNotificationInfo;
import java.util.List;
import java.util.ArrayList;

public class MBeanExporter {
    private final MBeanServer server;

    public MBeanExporter(MBeanServer server) {
        this.server = server;
    }

    public void export(String name, Object object) {
        try {
            ObjectName objectName = new ObjectName(name);

            MBeanInfoBuilder builder = new MBeanInfoBuilder();
            ModelMBeanInfo info = builder.buildInfo(object.getClass());

            // Use the GetterStrippingMBeanInfo to work around http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6339571.
            RequiredModelMBean mbean = new RequiredModelMBean(new GetterStrippingMBeanInfo(info));
            mbean.setManagedResource(object, "objectReference");

            server.registerMBean(mbean, objectName);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static class GetterStrippingMBeanInfo extends ModelMBeanInfoSupport {
        
        public GetterStrippingMBeanInfo(ModelMBeanInfo info) {
            super(info);
        }

        @Override
        public GetterStrippingMBeanInfo clone() {
            return new GetterStrippingMBeanInfo(this);
        }

        /**
         * Get rid of getter and setter operations as the mbeaninfo is serialized
         * Clients will see just the attributes
         *
         * @return
         */
        private Object writeReplace() {
            List<MBeanOperationInfo> operations = new ArrayList<MBeanOperationInfo>();

            for (MBeanOperationInfo operation : getOperations()) {
    //            Descriptor descriptor = operation.getDescriptor();
    //            String role = (String) descriptor.getFieldValue("role");
    //            if (!"getter".equalsIgnoreCase(role) && !"setter".equalsIgnoreCase(role)) {
                operations.add(operation);
    //            }
            }

            try {
                return new ModelMBeanInfoSupport(
                        this.getClassName(),
                        this.getDescription(),
                        (ModelMBeanAttributeInfo[]) this.getAttributes(),
                        (ModelMBeanConstructorInfo[]) this.getConstructors(),
                        operations.toArray(new ModelMBeanOperationInfo[0]),
                        (ModelMBeanNotificationInfo[]) this.getNotifications(),
                        this.getMBeanDescriptor());
            }
            catch (MBeanException e) {
                throw new RuntimeException(e);
            }
        }
    }
}