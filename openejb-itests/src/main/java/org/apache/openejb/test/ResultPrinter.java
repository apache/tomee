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
package org.apache.openejb.test;

import java.io.PrintStream;
import java.util.Enumeration;

import junit.framework.TestFailure;
import junit.framework.TestResult;

/**
 *
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 */
public class ResultPrinter extends junit.textui.ResultPrinter {

    public ResultPrinter(PrintStream writer) {
            super(writer);
    }

    public PrintStream writer(){
        return getWriter();
    }

    public void printFailures(TestResult result) {
        if (result.failureCount() != 0) {
            writer().println("\n~~ Failure Results ~~~~~~~~~\n");
            if (result.failureCount() == 1)
                writer().println("There was " + result.failureCount() + " failure:");
            else
                writer().println("There were " + result.failureCount() + " failures:");
            
            int i = 1;
            writer().println("\nFailure Summary:");
            for (Enumeration e= result.failures(); e.hasMoreElements(); i++) {
                TestFailure failure= (TestFailure) e.nextElement();
                writer().println(i + ") " + failure.failedTest());
            }
            i = 1;
            writer().println("\nFailure Details:");
            for (Enumeration e= result.failures(); e.hasMoreElements(); i++) {
                TestFailure failure= (TestFailure) e.nextElement();
                writer().println("\n"+ i + ") " + failure.failedTest());
                Throwable t= failure.thrownException();
                if (t.getMessage() != null)
                    writer().println("\t\"" + t.getMessage() + "\"");
                else {
                    writer().println();
                    failure.thrownException().printStackTrace();
                }
            }
        }
    }

    /**
     * Prints the header of the report
     */
    public void printHeader(TestResult result) {
        if (result.wasSuccessful()) {
            writer().println();
            writer().print("OK");
            writer().println (" (" + result.runCount() + " tests)");
    
        } else {
            writer().println();
            writer().println("FAILURES!!!");
            writer().println("~~ Test Results ~~~~~~~~~~~~");
            writer().println("      Run: "+result.runCount());
            writer().println(" Failures: "+result.failureCount());
            writer().println("   Errors: "+result.errorCount());
        }
    }

    public void printErrors(TestResult result) {
        if (result.errorCount() != 0) {
            writer().println("\n~~ Error Results ~~~~~~~~~~~\n");
            if (result.errorCount() == 1)
                writer().println("There was "+result.errorCount()+" error:");
            else
                writer().println("There were "+result.errorCount()+" errors:");
    
            writer().println("\nError Summary:");
            int i = 1;
            for (Enumeration e= result.errors(); e.hasMoreElements(); i++) {
                TestFailure failure= (TestFailure) e.nextElement();
                writer().println(i + ") " + failure.failedTest());
            }
            writer().println("\nError Details:");
            i = 1;
            for (Enumeration e= result.errors(); e.hasMoreElements(); i++) {
                TestFailure failure= (TestFailure)e.nextElement();
                writer().println(i+") "+failure.failedTest());
                String trace = getRelevantStackTrace(failure.thrownException());
                writer().println(trace);
            }
        }
    }

    public String getRelevantStackTrace(Throwable t){
        StringBuffer trace = new StringBuffer();
        
        try{
            // Cut the stack trace after "at junit.framework" is found
            // Return just the first part.
            java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
            java.io.PrintWriter pw = new java.io.PrintWriter(bos);
            t.printStackTrace(pw);
            pw.close();
    
            java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.StringReader(bos.toString()));
            String line = reader.readLine();
            while(line != null) {
                if (line.indexOf("at junit.framework") != -1) break;
                if (line.indexOf("at org.apache.openejb.test.NumberedTestCase") != -1) break;
                if (line.indexOf("at org.apache.openejb.test.TestSuite") != -1) break;
                
                trace.append(line).append('\n');
                line = reader.readLine();
            }
        } catch(Exception e){
        }
        
        return trace.toString();
    }

}
