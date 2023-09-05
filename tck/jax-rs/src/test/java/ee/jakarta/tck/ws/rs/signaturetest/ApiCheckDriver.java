/*
 * Copyright (c) 2007, 2020 Oracle and/or its affiliates. All rights reserved.
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

/*
 * $Id$
 */
package ee.jakarta.tck.ws.rs.signaturetest;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintWriter;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

import ee.jakarta.tck.ws.rs.lib.util.TestUtil;

public final class ApiCheckDriver extends SignatureTestDriver
    implements Serializable {

  /* flags for the Diff utility argument list */
  private static final String BASE_FLAG = "-base";

  private static final String TEST_FLAG = "-test";

  private static final String PACKAGE_NO_SUBS_FLAG = "-PackageWithoutSubpackages";

  private static final String PACKAGE_FLAG = "-package";

  private static final String EXPACKAGE_FLAG = "-expackage";

  private static final String REFLECT_FLAG = "-reflect";

  private static final String CONST_FLAG = "-constvalues";

  // ---------------------------------------- Methods from SignatureTestDriver

  @Override
  protected String normalizeFileName(File f) {
    return f.getPath();
  }

  @Override
  protected String[] createTestArguments(String packageListFile, String mapFile,
      String signatureRepositoryDir, String packageOrClassUnderTest,
      String classpath, boolean bStaticMode) throws Exception {

    Class pkgListClass = Class.forName("javasoft.sqe.apiCheck.PackageList");
    Constructor pkgCtor = pkgListClass
        .getDeclaredConstructor(new Class[] { String.class });
    Object pkgInstance = pkgCtor.newInstance(new Object[] { packageListFile });

    Method pkgMethod = pkgListClass.getDeclaredMethod("getSubPackagesFormatted",
        new Class[] { String.class });

    String excludePkgs = (String) pkgMethod.invoke(pkgInstance,
        new Object[] { packageOrClassUnderTest });

    List sigArgsList = new LinkedList();

    sigArgsList.add(BASE_FLAG);
    sigArgsList.add(
        getSigFileInfo(packageOrClassUnderTest, mapFile, signatureRepositoryDir)
            .getFile());

    if (classpath != null && classpath.length() > 0) {
      sigArgsList.add(TEST_FLAG);
      sigArgsList.add(classpath);
    }

    sigArgsList.add(REFLECT_FLAG);
    sigArgsList.add(CONST_FLAG);
    sigArgsList.add(PACKAGE_FLAG);
    sigArgsList.add(packageOrClassUnderTest);

    if (excludePkgs != null && excludePkgs.length() > 0) {
      sigArgsList.add(EXPACKAGE_FLAG);
      sigArgsList.add(excludePkgs);
    }

    return (String[]) (sigArgsList.toArray(new String[sigArgsList.size()]));

  } // END createTestArguments

  @Override
  protected boolean runSignatureTest(String packageOrClassName,
      String[] testArguments) throws Exception {

    Class diffClass = Class.forName("javasoft.sqe.apiCheck.Diff");
    Method mainMethod = diffClass.getDeclaredMethod("main",
        new Class[] { String[].class });
    mainMethod.invoke(null, new Object[] { testArguments });

    Method diffMethod = diffClass.getDeclaredMethod("diffsFound",
        new Class[] {});
    return (!((Boolean) diffMethod.invoke(null, new Object[] {}))
        .booleanValue());

  } // END runSignatureTest

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

  @Override
  protected boolean verifyJTAJarForNoXA(String classpath, String repositoryDir) 
      throws Exception  {
    // Need to find out whether implementing this method is really required now.
    // By default, signature test framework will use sigtest
    return true;
  }

} // END ApiCheckDriver
