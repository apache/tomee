/**
 *
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
package org.apache.openejb.server.axis.assembler;

import javax.xml.namespace.QName;

public class JaxRpcParameterInfo {
    public QName qname;
    public Mode mode;
    public boolean soapHeader;
    public QName xmlType;
    public String javaType;

    public static enum Mode {
        IN(true, false),
        OUT(false, true),
        INOUT(true, true);

        private boolean in;
        private boolean out;

        Mode(boolean in, boolean out) {
            this.in = in;
            this.out = out;
        }

        public boolean isIn() {
            return in;
        }

        public void setIn(boolean in) {
            this.in = in;
        }

        public boolean isOut() {
            return out;
        }

        public void setOut(boolean out) {
            this.out = out;
        }
    }
}
