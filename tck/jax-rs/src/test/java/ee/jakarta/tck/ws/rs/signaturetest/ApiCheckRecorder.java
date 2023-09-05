/*
 * Copyright (c) 2007, 2018 Oracle and/or its affiliates. All rights reserved.
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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * This implementation of {@link Recorder} will record signatures using the
 * <code>ApiCheck</code> framework.
 * </p>
 */
public class ApiCheckRecorder extends Recorder {

  // ------------------------------------------------------------ Constructors

  public ApiCheckRecorder(String[] args) {

    super(args);
    System.setProperty("pkg.list.file.path", packageListFile);
    System.setProperty("map.file.path", signatureMapFile);
    System.setProperty("signature.repository.dir", signatureRepositoryDir);

  } // END ApiCheckRecorder

  // ------------------------------------------------------- Protected Methods

  protected String[] createCommandLine(String version, String classpath,
      String outputFileName, String packageName) {

    List command = new ArrayList();

    command.add("-constvalues");
    command.add("-xpriv");

    command.add("-in");
    command.add(classpath);

    return ((String[]) command.toArray(new String[command.size()]));

  } // END getCommandLine

  protected void writePackageListFile(String basePackageName,
      String signatureFile, String packageListFile) throws Exception {

    // no-op as this is done internally by our version of ApiCheck

  } // END writePackageListFile

  protected void doRecord(String[] commandLine) throws Exception {

    Class batchSetup = Class.forName("javasoft.sqe.apiCheck.BatchSetup");
    Method mainMethod = batchSetup.getDeclaredMethod("main",
        new Class[] { String[].class });
    mainMethod.invoke(null, new Object[] { commandLine });

  } // END doRecord

} // END SigTestRecorder
