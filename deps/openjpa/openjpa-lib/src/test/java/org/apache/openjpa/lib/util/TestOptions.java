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
package org.apache.openjpa.lib.util;

import java.util.Properties;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 * Tests the {@link Options} type.
 *
 * @author Abe White
 */
public class TestOptions extends TestCase {

    private Options _opts = null;

    private String[] _args = new String[]{
        "-int", "10", "-boolean", "-string", "STR,STR2", "-range1", "10,100",
        "-range2", "10", "-fieldVal", "20", "-FieldVal2", "30",
        "-inner.nullInner.range1", "10,100", "arg1", "arg2", "arg3" };

    public TestOptions(String test) {
        super(test);
    }

    public void setUp() {
        Properties defs = new Properties();
        defs.setProperty("default", "value");
        _opts = new Options(defs);
        _args = _opts.setFromCmdLine(_args);
    }

    /**
     * Test command-line parsing.
     */
    public void testCmdLineParsing() {
        assertEquals(3, _args.length);
        assertEquals("arg1", _args[0]);
        assertEquals("arg2", _args[1]);
        assertEquals("arg3", _args[2]);

        assertEquals("10", _opts.getProperty("int"));
        assertEquals("true", _opts.getProperty("boolean"));
        assertEquals("STR,STR2", _opts.getProperty("string"));
        assertEquals("20", _opts.getProperty("fieldVal"));
        assertEquals("30", _opts.getProperty("FieldVal2"));
        assertEquals("10,100", _opts.getProperty("range1"));
        assertEquals("10", _opts.getProperty("range2"));
        assertEquals("10,100", _opts.getProperty("inner.nullInner.range1"));
        assertEquals("value", _opts.getProperty("default"));

        _args = _opts.setFromCmdLine(new String[]{ "-default", "newValue" });
        assertEquals(0, _args.length);
        assertEquals("newValue", _opts.getProperty("default"));
    }

    /**
     * Tests the setting of option values into bean objects.
     */
    public void testSetObject() {
        Inner inner = new Inner();
        _opts.setInto(inner);

        assertEquals(10, inner.getInt());
        assertTrue(inner.getBoolean());
        assertEquals("STR,STR2", inner.getString());
        assertEquals(20, inner.fieldVal);
        assertEquals(30, inner.fieldVal2);
        assertEquals(10, inner.getRange1()[0]);
        assertEquals(100, inner.getRange1()[1]);
        assertEquals(10, inner.getRange2()[0]);
        assertEquals(0, inner.getRange2()[1]);
        assertEquals("value", inner.getDefault());

        assertEquals(10, inner.getInner().getNullInner().getRange1()[0]);
        assertEquals(100, inner.getInner().getNullInner().getRange1()[1]);

        inner = new Inner();
        Options opts = new Options();
        opts.setProperty("inner", Inner2.class.getName());
        opts.setInto(inner);
        assertEquals(Inner2.class, inner.getInner().getClass());

        inner = new Inner();
        opts = new Options();
        opts.setProperty("mixed", "STR,1");
        opts.setInto(inner);
        assertEquals(1, inner.getInt());
        assertEquals("STR", inner.getString());
    }

    public static Test suite() {
        return new TestSuite(TestOptions.class);
    }

    public static void main(String[] args) {
        TestRunner.run(suite());
    }

    /**
     * Used internally for testing; must be public so Options can construct it.
     */
    public static class Inner {

        public int fieldVal = 0;
        public int fieldVal2 = 0;

        private int _int = 0;
        private boolean _boolean = false;
        private String _string = null;
        private String _default = null;
        private Inner _inner = null;
        private Inner _nullInner = null;
        private int[] _range1 = new int[2];
        private int[] _range2 = new int[2];

        public Inner() {
        }

        public int getInt() {
            return _int;
        }

        public void setInt(int i) {
            _int = i;
        }

        public boolean getBoolean() {
            return _boolean;
        }

        public void setBoolean(boolean b) {
            _boolean = b;
        }

        public String getString() {
            return _string;
        }

        public void setString(String s) {
            _string = s;
        }

        public String getDefault() {
            return _default;
        }

        public void setDefault(String s) {
            _default = s;
        }

        public int[] getRange1() {
            return _range1;
        }

        public void setRange1(int min, int max) {
            _range1[0] = min;
            _range1[1] = max;
        }

        public int[] getRange2() {
            return _range2;
        }

        public void setRange2(int min, int max) {
            _range2[0] = min;
            _range2[1] = max;
        }

        public void setMixed(String s, int i) {
            _int = i;
            _string = s;
        }

        public Inner getInner() {
            if (_inner == null)
                _inner = new Inner();
            return _inner;
        }

        public void setInner(Inner in) {
            _inner = in;
        }

        public Inner getNullInner() {
            return _nullInner;
        }

        public void setNullInner(Inner in) {
            _nullInner = in;
        }
    }

    /**
     * Used internally for testing; must be public so Options can construct it.
     */
    public static class Inner2 extends Inner {

    }
}
