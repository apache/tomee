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
package org.apache.openejb;

import java.util.List;
import java.util.ArrayList;
import java.io.PrintStream;

/**
 * @version $Rev$ $Date$
 */
public class UndeployException extends OpenEJBException {

    private final List<Throwable> causes = new ArrayList<Throwable>();

    public UndeployException() {
    }

    public UndeployException(String message) {
        super(message);
    }

    public UndeployException(String message, Throwable rootCause) {
        super(message, rootCause);
    }

    public UndeployException(Throwable rootCause) {
        super(rootCause);
    }

    public List<Throwable> getCauses() {
        return causes;
    }

    public void printStackTrace(PrintStream s) {
        super.printStackTrace(s);

        for (Throwable throwable : causes) {
            s.print("Nested caused by: ");
            throwable.printStackTrace(s);
        }
    }
}
