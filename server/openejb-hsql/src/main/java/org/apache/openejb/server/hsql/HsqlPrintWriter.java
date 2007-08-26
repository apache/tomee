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
package org.apache.openejb.server.hsql;

import java.io.PrintWriter;

import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;

public class HsqlPrintWriter extends PrintWriter {
	private Logger logger;
	private boolean errorWriter;
	private StringBuffer text  = new StringBuffer("");
	public HsqlPrintWriter(boolean errorWriter){
		super(System.err);
		logger = Logger.getInstance(LogCategory.OPENEJB_HSQL,HsqlPrintWriter.class);
		this.errorWriter = errorWriter;
	}
    public void close() {
        flush();
    }

    private void flushLine() {
        if(!errorWriter)
        	logger.info(text.toString());
        else
        	logger.error(text.toString());
        text.setLength(0);
    }

    public void flush() {
        if (!text.toString().equals("")) {
            flushLine();
        }
    }

    public void print(boolean b) {
        text.append(b);
    }

    public void print(char c) {
        text.append(c);
    }

    public void print(char[] s) {
        text.append(s);
    }

    public void print(double d) {
        text.append(d);
    }

    public void print(float f) {
        text.append(f);
    }

    public void print(int i) {
        text.append(i);
    }

    public void print(long l) {
        text.append(l);
    }

    public void print(Object obj) {
        text.append(obj);
    }

    public void print(String s) {
        text.append(s);
    }

    public void println() {
        if (!text.toString().equals("")) {
            flushLine();
        }
    }

    public void println(boolean x) {
        text.append(x);
        flushLine();
    }

    public void println(char x) {
        text.append(x);
        flushLine();
    }

    public void println(char[] x) {
        text.append(x);
        flushLine();
    }

    public void println(double x) {
        text.append(x);
        flushLine();
    }

    public void println(float x) {
        text.append(x);
        flushLine();
    }

    public void println(int x) {
        text.append(x);
        flushLine();
    }

    public void println(long x) {
        text.append(x);
        flushLine();
    }

    public void println(Object x) {
        text.append(x);
        flushLine();
    }

    public void println(String x) {
        text.append(x);
        flushLine();
    }

}
