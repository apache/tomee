/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */
package org.apache.openjpa.persistence.common.utils;

import java.util.*;

import org.apache.openjpa.lib.log.*;

/**
 * Simple default log implementation to test whether certain messages
 * are logged or not.
 *
 * @author Marc Prud'hommeaux
 */
public class BufferedLogFactory
    extends LogFactoryImpl {

    private int bufferSize = 10000;
    private List<String> buffer = new ArrayList<String>();
    private List<String> disallowedMessages = new LinkedList<String>();

    protected LogImpl newLogImpl() {
        return new BufferedLog();
    }

    public List<String> getBuffer() {
        return Collections.unmodifiableList(buffer);
    }

    public void clear() {
        buffer.clear();
    }

    public void clearDisallowedMessages() {
        disallowedMessages.clear();
    }

    public void addDisallowedMessage(String regexp) {
        disallowedMessages.add(regexp);
    }

    public boolean removeDisallowedMessage(String regexp) {
        return disallowedMessages.remove(regexp);
    }

    public List<String> getDisallowedMessages() {
        return Collections.unmodifiableList(disallowedMessages);
    }

    public void assertLogMessage(String regex) {
    	AbstractTestCase.assertMatches(regex, getBuffer());
    }

    public void assertNoLogMessage(String regex) {
    	AbstractTestCase.assertNotMatches(regex, getBuffer());
    }

    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    public int getBufferSize() {
        return this.bufferSize;
    }

    public class BufferedLog
        extends LogFactoryImpl.LogImpl {

        protected void log(short level, String message, Throwable t) {
            super.log(level, message, t);
            buffer.add(message);

            // trim to max buffer length
            while (buffer.size() > getBufferSize())
                buffer.iterator().remove();

            if (disallowedMessages.size() > 0) {
                for (Iterator<String> i = disallowedMessages.iterator(); i.hasNext();) {
                    String regex = i.next();
                    AbstractTestCase.assertNotMatches(regex, message);
                }
            }
        }
    }
}
