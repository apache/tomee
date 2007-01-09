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
package org.apache.openejb.core.stateful;

import javax.naming.NameNotFoundException;

import org.apache.openejb.core.CoreUserTransaction;
import org.apache.openejb.core.Operation;
import org.apache.openejb.core.ivm.naming.EncReference;
import org.apache.openejb.core.ivm.naming.ObjectReference;


/**
 * This class is a wrapper for CoreUserTransaction reference in the
 * JNDI ENC of a stateful bean.  When the getObject( ) method is invoked the
 * Operation is checked to ensure that its is allowed for the bean's current state.
 */
public class StatefulEncUserTransaction extends EncReference {

    /**
     * This constructor take a new CoreUserTransaction object as the object reference
     */
    public StatefulEncUserTransaction(CoreUserTransaction reference) {
        super(new ObjectReference(reference));
    }

    /**
     * This method is invoked by the EncReference super class each time its
     * getObject() method is called within the container system.  This checkOperation
     * method ensures that the stateful bean is in the correct state before the super
     * class can return the requested reference object.
     */
    public void checkOperation(Operation operation) throws NameNotFoundException {
        if (operation == Operation.OP_SET_CONTEXT || operation == Operation.OP_AFTER_COMPLETION || operation == Operation.OP_BEFORE_COMPLETION) {
            throw new NameNotFoundException("Operation Not Allowed");
        }
    }
}
