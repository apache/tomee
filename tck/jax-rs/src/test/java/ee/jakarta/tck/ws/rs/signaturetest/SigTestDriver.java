/*
 * Copyright (c) 2007, 2021 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package ee.jakarta.tck.ws.rs.signaturetest;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import ee.jakarta.tck.ws.rs.lib.util.TestUtil;

/**
 * <p>
 * Wrapper for the <code>Sig Test</code> framework.
 * </p>
 */
public class SigTestDriver extends SignatureTestDriver {

  private static final String CLASSPATH_FLAG = "-Classpath";

  private static final String FILENAME_FLAG = "-FileName";

  private static final String PACKAGE_FLAG = "-Package";

  private static final String PACKAGE_NO_SUBS_FLAG = "-PackageWithoutSubpackages";

  private static final String API_VERSION_FLAG = "-ApiVersion";

  private static final String EXCLUDE_FLAG = "-Exclude";
  
  private static final String STATIC_FLAG = "-Static";

  private static final String CHECKVALUE_FLAG = "-CheckValue"; // only valid w/
                                                               // -static

  private static final String NO_CHECKVALUE_FLAG = "-NoCheckValue";

  private static final String SMODE_FLAG = "-mode"; // requires arg of bin or
                                                    // src

  private static final String DEBUG_FLAG = "-Debug";

  private static final String FORMATPLAIN_FLAG = "-FormatPlain";
  
  private static final String EXCLUDE_JDK_CLASS_FLAG = "-IgnoreJDKClass";

  private static String[] excludeJdkClasses = {
          "java.util.Map",
          "java.lang.Object",
          "java.io.ByteArrayInputStream",
          "java.io.InputStream",
          "java.lang.Deprecated",
          "java.io.Writer",
          "java.io.OutputStream",
          "java.util.List",
          "java.util.Collection",
          "java.lang.instrument.IllegalClassFormatException",
          "javax.transaction.xa.XAException",
          "java.lang.annotation.Repeatable",
          "java.lang.InterruptedException",
          "java.lang.CloneNotSupportedException",
          "java.lang.Throwable",
          "java.lang.Thread",
          "java.lang.Enum"
  };
          
  // ---------------------------------------- Methods from SignatureTestDriver

  @Override
  protected String normalizeFileName(File f) {
    String sURL = null;
    try {
      sURL = f.toURI().toURL().toExternalForm();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return sURL;
  }

  @Override
  protected String[] createTestArguments(String packageListFile, String mapFile,
      String signatureRepositoryDir, String packageOrClassUnderTest,
      String classpath, boolean bStaticMode) throws Exception {

    SignatureFileInfo info = getSigFileInfo(packageOrClassUnderTest, mapFile,
        signatureRepositoryDir);

    PackageList packageList = new PackageList(packageListFile);
    String[] subPackages = packageList.getSubPackages(packageOrClassUnderTest);

    List command = new ArrayList();

    if (bStaticMode) {
      // static mode allows finer level of constants checking
      // -CheckValue says to check the actual const values
      TestUtil.logTrace("Setting static mode flag to allow constant checking.");
      command.add(STATIC_FLAG);
      command.add(CHECKVALUE_FLAG);

      // specifying "-mode src" allows stricter 2 way verification of constant
      // vals
      // (note that using "-mode bin" mode is less strict)
      command.add(SMODE_FLAG);
      // command.add("bin");
      command.add("src");
    } else {
      TestUtil
          .logTrace("Not Setting static mode flag to allow constant checking.");
    }

    if (TestUtil.harnessDebug) {
      command.add(DEBUG_FLAG);
    }
    command.add("-Verbose");

    command.add(FILENAME_FLAG);
    command.add(info.getFile());

    command.add(CLASSPATH_FLAG);
    command.add(classpath);

    command.add(PACKAGE_FLAG);
    command.add(packageOrClassUnderTest);

    for (int i = 0; i < subPackages.length; i++) {
      command.add(EXCLUDE_FLAG);
      command.add(subPackages[i]);
    }

    for(String jdkClassName:excludeJdkClasses) {
      command.add(EXCLUDE_JDK_CLASS_FLAG);
      command.add(jdkClassName);
    }
    
            
    command.add(API_VERSION_FLAG);
    command.add(info.getVersion());

    return ((String[]) command.toArray(new String[command.size()]));

  } // END createTestArguments

  @Override
  protected boolean runSignatureTest(String packageOrClassName,
      String[] testArguments) throws Exception {

    Class sigTestClass = Class
        .forName("com.sun.tdk.signaturetest.SignatureTest");
    Object sigTestInstance = sigTestClass.newInstance();

    ByteArrayOutputStream output = new ByteArrayOutputStream();

    // do some logging to help with troubleshooting
    TestUtil.logTrace(
        "\nCalling:  com.sun.tdk.signaturetest.SignatureTest() with following args:");
    for (int ii = 0; ii < testArguments.length; ii++) {
      TestUtil.logTrace("   testArguments[" + ii + "] = " + testArguments[ii]);
    }

    @SuppressWarnings("unchecked")
    Method runMethod = sigTestClass.getDeclaredMethod("run",
        new Class[] { String[].class, PrintWriter.class, PrintWriter.class });
    runMethod.invoke(sigTestInstance,
        new Object[] { testArguments, new PrintWriter(output, true), null });

    String rawMessages = output.toString();

    // currently, there is no way to determine if there are error msgs in
    // the rawmessages, so we will always dump this and call it a status.
    TestUtil.logMsg(
        "********** Status Report '" + packageOrClassName + "' **********\n");
    TestUtil.logMsg(rawMessages);

    return sigTestInstance.toString().substring(7).startsWith("Passed.");
  } // END runSignatureTest

  /*
   * 
   * @return This returns true if the packageOrClassName is found in the impl.
   */
  @Override
  protected boolean runPackageSearch(String packageOrClassName,
      String[] testArguments) throws Exception {

    Class sigTestClass = Class
        .forName("com.sun.tdk.signaturetest.SignatureTest");
    Object sigTestInstance = sigTestClass.newInstance();

    ByteArrayOutputStream output = new ByteArrayOutputStream();

    // we want to replace the PACKAGE_FLAG with PACKAGE_NO_SUBS_FLAG
    for (int ii = 0; ii < testArguments.length; ii++) {
      if (testArguments[ii].equals(PACKAGE_FLAG)) {
        testArguments[ii] = PACKAGE_NO_SUBS_FLAG;
      }
    }

    // dump args for debugging aid
    TestUtil.logTrace(
        "\nCalling:  com.sun.tdk.signaturetest.SignatureTest() with following args:");
    for (int ii = 0; ii < testArguments.length; ii++) {
      TestUtil.logTrace("	  testArguments[" + ii + "] = " + testArguments[ii]);
    }

    @SuppressWarnings("unchecked")
    Method runMethod = sigTestClass.getDeclaredMethod("run",
        new Class[] { String[].class, PrintWriter.class, PrintWriter.class });
    runMethod.invoke(sigTestInstance,
        new Object[] { testArguments, new PrintWriter(output, true), null });

    String rawMessages = output.toString();

    // currently, there is no way to determine if there are error msgs in
    // the rawmessages, so we will always dump this and call it a status.
    TestUtil.logMsg(
        "********** Status Report '" + packageOrClassName + "' **********\n");
    TestUtil.logMsg(rawMessages);

    return sigTestInstance.toString().substring(7).startsWith("Passed.");
  }

  /*
   * @return This returns true if javax.transaction.xa is not found in the
   * JTA API jar
   */
  protected boolean verifyJTAJarForNoXA(String classpath, String repositoryDir) throws Exception  {

    TestUtil.logMsg("SigTestDriver#verifyJTAJarForNoXA - Starting:");
    boolean result = false;
    List command = new ArrayList();

    // Build Commandline for com.sun.tdk.signaturetest.SignatureTest
    command.add(STATIC_FLAG);
    command.add(FILENAME_FLAG);
    command.add(repositoryDir  + "empty.sig");
    command.add(PACKAGE_FLAG);
    command.add("javax.transaction.xa");
    command.add(CLASSPATH_FLAG);
    command.add(classpath);

    String testArguments [] = (String[]) command.toArray(new String[command.size()]);

    // do some logging to help with troubleshooting
    TestUtil.logTrace("\nCalling:  com.sun.tdk.signaturetest.SignatureTest() with following args:");
    for (int ii=0; ii < testArguments.length; ii++) {
      TestUtil.logTrace("   testArguments[" +ii+ "] = " + testArguments[ii]);
    }

    Class sigTestClass = Class.forName("com.sun.tdk.signaturetest.SignatureTest");
    Object sigTestInstance = sigTestClass.newInstance();
    ByteArrayOutputStream output = new ByteArrayOutputStream();

    @SuppressWarnings("unchecked")
    Method runMethod = sigTestClass.getDeclaredMethod("run",
                                           new Class[] { String[].class,
                                                         PrintWriter.class,
                                                         PrintWriter.class });
    runMethod.invoke(sigTestInstance,
                         new Object[] { testArguments, 
                                        new PrintWriter(output, true), 
                                        null });
    String rawMessages = output.toString();

    // currently, there is no way to determine if there are error msgs in 
    // the rawmessages, so we will always dump this and call it a status.
    TestUtil.logMsg("********** Status Report JTA JAR validation **********\n");
    TestUtil.logMsg(rawMessages);
    return sigTestInstance.toString().substring(7).startsWith("Passed.");
  }
}
