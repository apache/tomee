/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.superbiz.resource.jmx.factory;

public enum PrimitiveTypes {
    BOOLEAN {
        @Override
        public String getDefaultValue() {
            return "false";
        }

        @Override
        public Class<?> getWraper() {
            return Boolean.class;
        }
    },
    BYTE {
        @Override
        public String getDefaultValue() {
            return "0";
        }

        @Override
        public Class<?> getWraper() {
            return Byte.class;
        }
    },
    CHAR {
        @Override
        public String getDefaultValue() {
            return "\u0000";
        }

        @Override
        public Class<?> getWraper() {
            return Character.class;
        }
    },
    CHARACTER {
        @Override
        public String getDefaultValue() {
            return "\u0000";
        }

        @Override
        public Class<?> getWraper() {
            return Character.class;
        }
    },
    LONG {
        @Override
        public String getDefaultValue() {
            return "0";
        }

        @Override
        public Class<?> getWraper() {
            return Long.class;
        }
    },
    FLOAT {
        @Override
        public String getDefaultValue() {
            return "0";
        }

        @Override
        public Class<?> getWraper() {
            return Float.class;
        }
    },
    INT {
        @Override
        public String getDefaultValue() {
            return "0";
        }

        @Override
        public Class<?> getWraper() {
            return Integer.class;
        }
    },
    DOUBLE {
        @Override
        public String getDefaultValue() {
            return "0";
        }

        @Override
        public Class<?> getWraper() {
            return Double.class;
        }
    },
    SHORT {
        @Override
        public String getDefaultValue() {
            return "0";
        }

        @Override
        public Class<?> getWraper() {
            return Short.class;
        }
    };

    public abstract String getDefaultValue();

    public abstract Class<?> getWraper();
}