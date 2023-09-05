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
 * $Id$
 */

package ee.jakarta.tck.ws.rs.signaturetest;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Properties;

import ee.jakarta.tck.ws.rs.lib.util.TestUtil;

public abstract class Recorder {

  protected Properties signatureMap;

  protected String packageListFile;

  protected String classpath;

  protected String signatureRepositoryDir;

  protected String signatureMapFile;

  // ------------------------------------------------------------ Constructors

  public Recorder(String[] args) {

    TestUtil.logTrace("\nCalling:  Recorder with following args:");
    for (int ii = 0; ii < args.length; ii++) {
      TestUtil.logTrace("	  args[" + ii + "] = " + args[ii]);
    }

    Arguments arguments = new Arguments(args);
    packageListFile = arguments.getPackageList();
    classpath = arguments.getClasspath();
    signatureRepositoryDir = arguments.getRepository();
    signatureMapFile = arguments.getSignatureMap();
    loadSignatureMap(signatureMapFile);
  }

  // ---------------------------------------------------------- Public Methods

  /**
   * <p>
   * Record the signatures for each package listed in the
   * <code>TS_HOME/bin/sig-test.map</code> file.
   * </p>
   */
  public void batchRecord() {

    for (Iterator i = signatureMap.keySet().iterator(); i.hasNext();) {
      String basePackageName = (String) i.next();
      String version = (String) signatureMap.get(basePackageName);
      String outputFileName = getOutputFileName(basePackageName, version);
      String[] commandLine = createCommandLine(version, classpath,
          outputFileName, basePackageName);

      try {

        // dump command line args passed to Setup()...
        TestUtil.logTrace("\n\nDUMPING SIGTEST COMMAND LINE: \n");
        for (int ii = 0; ii < commandLine.length; ii++) {
          TestUtil.logTrace("commandLine[" + ii + "] = " + commandLine[ii]);
        }
        TestUtil.logTrace("\nDONE DUMPING SIGTEST COMMAND LINE. \n\n");

        doRecord(commandLine);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }

      try {
        writePackageListFile(basePackageName, outputFileName, packageListFile);
      } catch (Exception e) {
        System.out.println("Unexpected exception: " + e);
        e.printStackTrace();
        System.exit(1);
      }
    }

  } // END batchRecord

  // ------------------------------------------------------- Protected Methods

  /**
   * Write, to a separate file, all of the packages that were recorded. How this
   * is accomplised will be dependent on the framework.
   * 
   * @param basePackageName
   *          the base package
   * @param signatureFile
   *          the file in which the signatures were recorded to
   * @param packageListFile
   *          the name of the package list file (which may or may not exist)
   * @throws Exception
   *           if an error occurs writing the file
   */
  protected abstract void writePackageListFile(String basePackageName,
      String signatureFile, String packageListFile) throws Exception;

  /**
   * Create a array of arguments appropriate for use with different signature
   * recording frameworks.
   * 
   * @param version
   *          The version of the API
   * @param classpath
   *          the classpath containing classes that will be recorded
   * @param outputFileName
   *          the file in which to write the recorded signatures to
   * @param packageName
   *          the base package name of the signatures that will be recorded
   */
  protected abstract String[] createCommandLine(String version,
      String classpath, String outputFileName, String packageName);

  /**
   * Perform whatever action in necessary to do the actual recording of the
   * signatures.
   * 
   * @param commandLine
   *          the options to invoke the recording facility
   * @throws Exception
   *           if an error occurs during the record process
   */
  protected abstract void doRecord(String[] commandLine) throws Exception;

  // --------------------------------------------------------- Private Methods

  private void loadSignatureMap(String signatureTestMapFile) {

    signatureMap = new Properties();
    try {
      signatureMap.load(
          new BufferedInputStream(new FileInputStream(signatureTestMapFile)));
    } catch (FileNotFoundException fnfe) {
      throw new RuntimeException(
          "Unable to find or read file '" + signatureTestMapFile + '\'');
    } catch (IOException ioe) {
      throw new RuntimeException(
          "Error processing file '" + signatureTestMapFile + '\'', ioe);
    }

  } // END loadSignatureMap

  private String getOutputFileName(String name, String version) {

    StringBuffer fileName = new StringBuffer();
    fileName.append(signatureRepositoryDir).append(File.separatorChar);
    fileName.append(name).append(".sig_").append(version);
    return fileName.toString();

  } // END getOutputFileName

  // --------------------------------------------------- Static Nested Classes

  private static class Arguments {

    private static final String CLASSPATH_ARG = "-classpath";

    private static final String PKG_LIST_ARG = "-packagelist";

    private static final String SIG_MAP_ARG = "-sigmap";

    private static final String REPOSITORY_ARG = "-repository";

    private String classpath;

    private String packageList;

    private String signatureMap;

    private String repository;

    // -------------------------------------------------------- Constructors

    Arguments(String[] args) {

      // all 4 arguments must be defined, thus there should be 8 elements
      if (args.length != 8) {
        System.out
            .println("Error - incorrect number of args should be 8 but was:  "
                + args.length);
        System.out.println("Args passed in were:  ");
        for (int ii = 0; ii < args.length; ii++) {
          System.out.println("args[" + ii + "] = " + args[ii]);
        }

        throw new IllegalArgumentException();
      }

      String[] clonedArgs = (String[]) args.clone();
      Arrays.sort(clonedArgs);
      // ensure the proper arguments are specified
      if (Arrays.binarySearch(clonedArgs, CLASSPATH_ARG) < 0
          || Arrays.binarySearch(clonedArgs, PKG_LIST_ARG) < 0
          || Arrays.binarySearch(clonedArgs, SIG_MAP_ARG) < 0
          || Arrays.binarySearch(clonedArgs, REPOSITORY_ARG) < 0) {
        usage();
        System.exit(1);
      }

      for (int i = 0; i < args.length; i += 2) {
        if (CLASSPATH_ARG.equals(args[i])) {
          classpath = args[i + 1];
        } else if (PKG_LIST_ARG.equals(args[i])) {
          packageList = args[i + 1];
        } else if (SIG_MAP_ARG.equals(args[i])) {
          signatureMap = args[i + 1];
        } else if (REPOSITORY_ARG.equals(args[i])) {
          repository = args[i + 1];
        } else {
          // shouldn't get here
          usage();
          System.exit(1);
        }
      }

    } // END Arguments

    // ---------------------------------------------------------- Properties

    public String getClasspath() {

      return classpath;

    } // END getClasspath

    public String getPackageList() {

      return packageList;

    } // END getPackageList

    public String getSignatureMap() {

      return signatureMap;

    } // END getSignatureMap

    public String getRepository() {

      return repository;

    } // END getRepository

    private static void usage() {

      String usage = "Usage:"
          + "\t-classpath (classpath to JARs and/or classes under test)\n"
          + "\t-packageList (Reference to the sig-test-pkg-list.txt file)\n"
          + "\t-sigmap (Reference to the sig-test.map file)\n"
          + "\t-repository (Directory in which to write the recorded"
          + "\tsignatures to)\n\n";

      System.err.println(usage);

    } // END usage

  } // END Arguments

}
