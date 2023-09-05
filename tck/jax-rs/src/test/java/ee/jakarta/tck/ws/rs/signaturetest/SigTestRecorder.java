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

/*
 * @(#)SigTestRecorder.java	1.1 03/03/05
 */
package ee.jakarta.tck.ws.rs.signaturetest;

import java.io.File;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * This implementation of {@link Recorder} will record signatures using the
 * <code>Signature Test</code> framework.
 * </p>
 */
public class SigTestRecorder extends Recorder {

  // ------------------------------------------------------------ Constructors

  public SigTestRecorder(String[] args) {

    super(args);

  } // END SigTestRecorder

  // ------------------------------------------------------- Protected Methods

  protected String[] createCommandLine(String version, String classpath,
      String outputFileName, String packageName) {

    List command = new ArrayList();

    // command.add("-xReflection");
    command.add("-static");
    command.add("-debug");
    command.add("-verbose");
    command.add("-classpath");
    command.add(classpath);

    command.add("-FileName");
    try {
      command.add(new File(outputFileName).toURI().toURL().toExternalForm());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    command.add("-package");
    command.add(packageName);

    command.add("-apiVersion");
    command.add(version);

    return ((String[]) command.toArray(new String[command.size()]));

  } // END getCommandLine

  protected void writePackageListFile(String basePackageName,
      String signatureFile, String packageListFile) throws Exception {

    PackageList packageList = new PackageList(basePackageName, signatureFile,
        packageListFile);
    packageList.writePkgListFile();

  } // END writePackageListFile

  protected void doRecord(String[] commandLine) throws Exception {

    Class batchSetup = Class.forName("com.sun.tdk.signaturetest.Setup");
    Object batchSetupInstance = batchSetup.newInstance();
    Method runMethod = batchSetup.getDeclaredMethod("run",
        new Class[] { String[].class, PrintWriter.class, PrintWriter.class });

    runMethod.invoke(batchSetupInstance, new Object[] { commandLine,
        new PrintWriter(System.out, true), new PrintWriter(System.err, true) });

  } // END doRecord

} // END SigTestRecorder
