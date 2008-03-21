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
package org.apache.openejb.test.object;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @author <a href="mailto:Richard@Monson-Haefel.com">Richard Monson-Haefel</a>
 */
public class OperationsPolicy implements java.io.Externalizable {

    public static final int Context_getEJBHome           = 0;        
    public static final int Context_getCallerPrincipal   = 1;
    public static final int Context_isCallerInRole       = 2;    
    public static final int Context_getRollbackOnly      = 3;   
    public static final int Context_setRollbackOnly      = 4;   
    public static final int Context_getUserTransaction   = 5;
    public static final int Context_getEJBObject         = 6;   
    public static final int Context_getPrimaryKey        = 7;  
    public static final int JNDI_access_to_java_comp_env = 8;
    public static final int Context_lookup               = 11;
    public static final int Context_getTimerService      = 12;

    private boolean[] allowedOperations = new boolean[13];

    public OperationsPolicy() {
    }

    public OperationsPolicy(int[] operations) {
        for (int i=0; i < operations.length; i++) {
            allow( operations[i] );
        }
    }

    public void allow(int i) {
        if (i < 0 || i > allowedOperations.length - 1 ) return;
        allowedOperations[i] = true;
    }

    public boolean equals(Object object) {
        if ( !(object instanceof OperationsPolicy ) ) return false;

        OperationsPolicy that = (OperationsPolicy)object;
        for (int i=0; i < allowedOperations.length; i++) {
            if (this.allowedOperations[i] != that.allowedOperations[i]) return false;
        }

        return true;
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        for (int i=0; i < allowedOperations.length; i++) {
            out.writeBoolean( allowedOperations[i] );
        }
    }
    
    public void readExternal(ObjectInput in) throws IOException,ClassNotFoundException {
        for (int i=0; i < allowedOperations.length; i++) {
            allowedOperations[i] = in.readBoolean();
        }
    }

    public String toString() {
        String str = "";
        for (int i=0; i < allowedOperations.length; i++) {
            String operation = "(empty)";
            switch(i){
                case Context_getEJBHome: operation = "Context_getEJBHome"; break;
                case Context_getCallerPrincipal: operation = "Context_getCallerPrincipal"; break;
                case Context_isCallerInRole: operation = "Context_isCallerInRole"; break;
                case Context_getRollbackOnly: operation = "Context_getRollbackOnly"; break;
                case Context_setRollbackOnly: operation = "Context_setRollbackOnly"; break;
                case Context_getUserTransaction: operation = "Context_getUserTransaction"; break;
                case Context_getEJBObject: operation = "Context_getEJBObject"; break;
                case Context_getPrimaryKey: operation = "Context_getPrimaryKey"; break;
                case JNDI_access_to_java_comp_env: operation = "JNDI_access_to_java_comp_env"; break;
                case Context_lookup: operation = "Context_lookup"; break;
                case Context_getTimerService: operation = "Context_getTimerService"; break;
            }
            str += allowedOperations[i] ? "TRUE   " : "FALSE  ";
            str += operation + "\n";

        }
        return str;

    }
}
