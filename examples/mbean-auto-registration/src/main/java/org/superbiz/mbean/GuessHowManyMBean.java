/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.superbiz.mbean;

import javax.management.Description;
import javax.management.MBean;
import javax.management.ManagedAttribute;
import javax.management.ManagedOperation;

@MBean
@Description("play with me to guess a number")
public class GuessHowManyMBean {

    private int value = 0;

    @ManagedAttribute
    @Description("you are cheating!")
    public int getValue() {
        return value;
    }

    @ManagedAttribute
    public void setValue(int value) {
        this.value = value;
    }

    @ManagedOperation
    public String tryValue(int userValue) {
        if (userValue == value) {
            return "winner";
        }
        return "not the correct value, please have another try";
    }
}
