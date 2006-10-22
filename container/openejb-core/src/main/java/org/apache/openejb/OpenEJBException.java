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

public class OpenEJBException extends Exception {

    public OpenEJBException() {
        super();
    }

    public OpenEJBException(String message) {
        super(message);
    }

    public OpenEJBException(Throwable rootCause) {
        super(rootCause);
    }

    public OpenEJBException(String message, Throwable rootCause) {
        super(message, rootCause);
    }

    public String getMessage() {
        Throwable rootCause = getCause();
        if (rootCause != null) {
            return super.getMessage() + ": " + rootCause.getMessage();
        } else {
            return super.getMessage();
        }
    }

    public Throwable getRootCause() {
        return super.getCause();
    }
}
