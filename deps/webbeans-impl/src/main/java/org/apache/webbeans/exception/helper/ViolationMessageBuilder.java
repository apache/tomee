/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.webbeans.exception.helper;

public class ViolationMessageBuilder
{
    private StringBuilder violationMessage;

    private final String lineSeparator = System.getProperty("line.separator");

    public static ViolationMessageBuilder newViolation()
    {
        return new ViolationMessageBuilder();
    }

    public static ViolationMessageBuilder newViolation(String... text)
    {
        return new ViolationMessageBuilder().append(text);
    }

    public ViolationMessageBuilder append(String... text)
    {
        appendText(text, false);
        return this;
    }

    public ViolationMessageBuilder addLine(String... text)
    {
        if(text == null)
        {
            return this;
        }
        
        appendText(text, true);
        return this;
    }

    private void appendText(String[] text, boolean appendLineSeparator)
    {
        if(violationMessage == null)
        {
            violationMessage = new StringBuilder();
        }
        else if(appendLineSeparator)
        {
            violationMessage.append(lineSeparator);
        }

        for(String t : text)
        {
            violationMessage.append(t);
        }
    }

    public boolean containsViolation()
    {
        return violationMessage != null;
    }

    @Override
    public String toString()
    {
        return containsViolation() ? violationMessage.toString() : "no violation recorded";
    }
}
