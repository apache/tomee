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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.entity.cmp;

public class CmpFieldSetter implements InstanceOperation {
    private final CmpField field;

    public CmpFieldSetter(CmpField field) {
        this.field = field;
    }

    public Object invokeInstance(CmpInstanceContext ctx, Object[] args) {
        if (args.length != 1) {
            throw new IllegalArgumentException("CmpFieldSetter must be passed exactally one argument:" +
                    " fieldName=" + field.getName() + ", args.length=" + args.length);
        }

        Object value = args[0];
        field.setValue(ctx, value);
        return null;
    }
}
