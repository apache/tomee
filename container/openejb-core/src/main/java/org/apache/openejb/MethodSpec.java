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
package org.apache.openejb;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.Serializable;

/**
 * @version $Revision$ $Date$
 */
public class MethodSpec implements Comparable, Serializable {
    private static final long serialVersionUID = -1623511701541770312L;
    private static final int AFTER_OTHER = 1;
    private static final int BEFORE_OTHER = -1;
    private final String methodIntf;
    private final String methodName;
    private final String[] parameterTypes;

    public MethodSpec(String methodIntf, String methodName, String[] parameterTypes) {
        this.methodIntf = methodIntf;
        this.methodName = methodName;
        this.parameterTypes = parameterTypes;
    }

    public MethodSpec(String text) {
        Pattern p = Pattern.compile("(\\S+) (\\S+)\\((\\S*)\\)");
        Matcher m = p.matcher(text);
        if (!m.matches()) {
            throw new IllegalArgumentException("Text must match (\\S+) (\\S+)\\((\\S*)\\) : " + text);
        }
        String intfString = m.group(1);
        if (intfString.equals("all")) {
            methodIntf = null;
        } else {
            methodIntf = intfString;
        }
        methodName = m.group(2);
        String parameters = m.group(3);
        if (parameters.length() > 0) {
            parameterTypes = parameters.split(" *, *");
        } else {
            parameterTypes = null;
        }
    }

    public String getMethodIntf() {
        return methodIntf;
    }

    public String getMethodName() {
        return methodName;
    }

    public String[] getParameterTypes() {
        return parameterTypes;
    }

    public int hashCode() {
        int result = 17;
        result = 37 * result + methodIntf.hashCode();
        result = 37 * result + methodName.hashCode();
        for (int i = 0; i < parameterTypes.length; i++) {
            result = 37 * result + parameterTypes[i].hashCode();
        }
        return result;
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof MethodSpec)) {
            return false;
        }

        MethodSpec methodSpec = (MethodSpec) obj;
        return methodIntf.equals(methodSpec.methodIntf) &&
                methodName.equals(methodSpec.methodName) &&
                Arrays.equals(parameterTypes, methodSpec.parameterTypes);
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer();
        if (methodIntf != null) {
            buffer.append(methodIntf);
        } else {
            buffer.append("all");
        }
        buffer.append(" ").append(methodName).append('(');
        if (parameterTypes != null) {
            for (int i = 0; i < parameterTypes.length; i++) {
                String parameterType = parameterTypes[i];
                if (i > 0) {
                    buffer.append(',');
                }
                buffer.append(parameterType);
            }
        }
        buffer.append(')');
        return buffer.toString();
    }

    public boolean matches(String methodIntf, String methodName, String[] parameterTypes) {
        assert methodIntf != null;
        assert methodName != null;
        assert parameterTypes != null;
        if (this.methodIntf != null && !methodIntf.equals(this.methodIntf)) {
            //definitely wrong interface
            return false;
        }
        //our interface is not specified or matches.
        if (this.methodName.equals("*")) {
            return true;
        }
        if (!methodName.equals(this.methodName)) {
            //explicitly different method names
            return false;
        }
        //same method names.
        if (this.parameterTypes == null) {
            return true;
        }
        return Arrays.equals(parameterTypes, this.parameterTypes);
    }

    public int compareTo(Object o) {
        if (!(o instanceof MethodSpec)) {
            return -1;
        }
        if (this == o) {
            return 0;
        }
        MethodSpec other = (MethodSpec) o;
        if (parameterTypes != null) {
            if (other.parameterTypes == null) {
                //parameter types always come before no param types
                return BEFORE_OTHER;
            }
            //both have param types
            if (methodIntf != null) {
                if (other.methodIntf == null) {
                    //method intf comes before no method intf.
                    return BEFORE_OTHER;
                }
                //both have method interfaces
                int intfOrder = methodIntf.compareTo(other.methodIntf);
                if (intfOrder != 0) {
                    return intfOrder;
                }
                //same interfaces
                return compareMethod(other);
            }
            if (other.methodIntf != null) {
                //they have method intf, we don't, they are first
                return AFTER_OTHER;
            }
            //neither has methodIntf: sort by method name
            return compareMethod(other);
        }
        //we don't have param types
        if (other.parameterTypes != null) {
            //they do, they are first
            return AFTER_OTHER;
        }
        //neither has param types.
        //explicit method name comes first
        if (!methodName.equals("*")) {
            if (other.methodName.equals("*")) {
                return BEFORE_OTHER;
            }
            //both explicit method names.
            //explicit method interface comes first
            if (methodIntf != null) {
                if (other.methodIntf == null) {
                    return BEFORE_OTHER;
                }
                //both explicit method intf. sort by intf, then methodName
                int intfOrder = methodIntf.compareTo(other.methodIntf);
                if (intfOrder != 0) {
                    return intfOrder;
                }
                //same interfaces
                return methodName.compareTo(other.methodName);
            }
            if (other.methodIntf != null) {
                //they have explicit method inft, we dont, they are first
                return AFTER_OTHER;
            }
            //neither have explicit method intf.
            return methodName.compareTo(other.methodName);
        }
        //we don't have explicit method name
        if (!other.methodName.equals("*")) {
            //they do, they are first
            return AFTER_OTHER;
        }
        //neither has explicit method name
        if (methodIntf != null) {
            if (other.methodIntf == null) {
                return BEFORE_OTHER;
            }
            return methodIntf.compareTo(other.methodIntf);
        }
        if (other.methodIntf != null) {
            return AFTER_OTHER;
        }
        //neither has methodIntf or explicit methodName.  problem.
        throw new IllegalStateException("Cannot compare " + this + " and " + other);
    }

    private int compareMethod(MethodSpec other) {
        int methodOrder = methodName.compareTo(other.methodName);
        if (methodOrder != 0) {
            return methodOrder;
        }
        //same method name, sort by params lexicographically
        for (int i = 0; i < parameterTypes.length; i++) {
            if (i == other.parameterTypes.length) {
                //the other list is shorter, they are first
                return AFTER_OTHER;
            }
            int paramOrder = parameterTypes[i].compareTo(other.parameterTypes[i]);
            if (paramOrder != 0) {
                return paramOrder;
            }
        }
        //our list is shorter, we are first
        return BEFORE_OTHER;
    }
}
