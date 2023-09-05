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

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.PrintStream;

import java.util.ArrayList;
import java.util.Properties;

import ee.jakarta.tck.ws.rs.lib.util.TestUtil;

/**
 * This class should be extended by TCK developers that wish to create a set of
 * signature tests that run inside all the Java EE containers. Developers must
 * implement the getPackages method to specify which packages are to be tested
 * by the signature test framework within which container.
 */
public abstract class SigTestEE {

  String[] sVehicles;

  private Object theSharedObject;

  private Object theSharedObjectArray[];


  protected SignatureTestDriver driver;

  /**
   * <p>
   * Returns a {@link SignatureTestDriver} appropriate for the particular TCK
   * (using API check or the Signature Test Framework).
   * </p>
   *
   * <p>
   * The default implementation of this method will return a
   * {@link SignatureTestDriver} that will use API Check. TCK developers can
   * override this to return the desired {@link SignatureTestDriver} for their
   * TCK.
   */
  protected SignatureTestDriver getSigTestDriver() {

    if (driver == null) {
        driver = SignatureTestDriverFactory.getInstance(SignatureTestDriverFactory.SIG_TEST);
    }

    return driver;

  } // END getSigTestDriver

  /**
   * Returns the location of the package list file. This file denotes the valid
   * sub-packages of any package being verified in the signature tests.
   * <p/>
   * Sub-classes are free to override this method if they use a different path
   * or filename for their package list file. Most users should be able to use
   * this default implementation.
   *
   * @return String The path and name of the package list file.
   */
  protected String getPackageFile() {
    return getSigTestDriver().getPackageFileImpl(testInfo.getBinDir());
  }

  /**
   * Returns the path and name of the signature map file that this TCK uses when
   * conducting signature tests. The signature map file tells the signature test
   * framework which API versions of tested packages to use. To keep this code
   * platform independent, be sure to use the File.separator string (or the
   * File.separatorChar) to denote path separators.
   * <p/>
   * Sub-classes are free to override this method if they use a different path
   * or filename for their signature map file. Most users should be able to use
   * this default implementation.
   *
   * @return String The path and name of the signature map file.
   */
  protected String getMapFile() {
    return getSigTestDriver().getMapFileImpl(testInfo.getBinDir());
  }

  /**
   * Returns the directory that contains the signature files.
   * <p/>
   * Sub-classes are free to override this method if they use a different
   * signature repository directory. Most users should be able to use this
   * default implementation.
   *
   * @return String The signature repository directory.
   */
  protected String getRepositoryDir() {
    return getSigTestDriver().getRepositoryDirImpl(testInfo.getTSHome());
  }

  /**
   * Returns the list of Optional Packages which are not accounted for. By
   * 'unlisted optional' we mean the packages which are Optional to the
   * technology under test that the user did NOT specifically list for testing.
   * For example, with Java EE 7 implementation, a user could additionally opt
   * to test a JSR-88 technology along with the Java EE technology. But if the
   * user chooses NOT to list this optional technology for testing (via ts.jte
   * javaee.level prop) then this method will return the packages for JSR-88
   * technology with this method call.
   * <p/>
   * This is useful for checking for a scenarios when a user may have forgotten
   * to identify a whole or partial technology implementation and in such cases,
   * Java EE platform still requires testing it.
   * <p/>
   * Any partial or complete impl of an unlistedOptionalPackage sends up a red
   * flag indicating that the user must also pass tests for this optional
   * technology area.
   * <p/>
   * Sub-classes are free to override this method if they use a different
   * signature repository directory. Most users should be able to use this
   * default implementation - which means that there was NO optional technology
   * packages that need to be tested.
   *
   * @return ArrayList<String>
   */
  protected ArrayList<String> getUnlistedOptionalPackages() {
    return null;
  }

  /**
   * Returns the list of packages that must be tested by the signature test
   * framework. TCK developers must implement this method in their signature
   * test sub-class.
   *
   * @param vehicleName
   *          The name of the vehicle the signature tests should be conducted
   *          in. Valid values for this property are ejb, servlet, ejb and
   *          appclient.
   *
   * @return String[] A list of packages that the developer wishes to test using
   *         the signature test framework. If the developer does not wish to
   *         test any package signatures in the specified vehicle this method
   *         should return null.
   *         <p>
   *         Note, The proper way to insure that this method is not called with
   *         a vehicle name that has no package signatures to verify is to
   *         modify the vehicle.properties in the $TS_HOME/src directory. This
   *         file provides a mapping that maps test directories to a list of
   *         vehicles where the tests in those directory should be run. As an
   *         extra precaution users are encouraged to return null from this
   *         method if the specified vehicle has no package signatures to be
   *         verified within it.
   */
  protected abstract String[] getPackages(String vehicleName);

  /**
   * <p>
   * Returns an array of individual classes that must be tested by the signature
   * test framwork within the specified vehicle. TCK developers may override
   * this method when this functionality is needed. Most will only need package
   * level granularity.
   * </p>
   *
   * <p>
   * If the developer doesn't wish to test certain classes within a particular
   * vehicle, the implementation of this method must return a zero-length array.
   * </p>
   *
   * @param vehicleName
   *          The name of the vehicle the signature tests should be conducted
   *          in. Valid values for this property are ejb, servlet, ejb and
   *          appclient.
   *
   * @return an Array of Strings containing the individual classes the framework
   *         should test based on the specifed vehicle. The default
   *         implementation of this method returns a zero-length array no matter
   *         the vehicle specified.
   */
  protected String[] getClasses(String vehicleName) {

    return new String[] {};

  } // END getClasses

  protected SigTestData testInfo; // holds the bin.dir and vehicle properties

  /**
   * Called by the test framework to initialize this test. The method simply
   * retrieves some state information that is necessary to run the test when
   * when the test framework invokes the run method (actually the test1 method).
   *
   * @param args
   *          List of arguments passed to this test.
   * @param p
   *          Properties specified by the test user and passed to this test via
   *          the test framework.
   *
   * @throws Fault
   *           When an error occurs reading or saving the state information
   *           processed by this method.
   */
  public void setup() {
    try {
      TestUtil.logMsg("$$$ SigTestEE.setup() called");
      this.testInfo = new SigTestData();
      TestUtil.logMsg("$$$ SigTestEE.setup() complete");
    } catch (Exception e) {
      TestUtil.logErr("Unexpected exception " + e.getMessage());
    }
  }

  /**
   * Called by the test framework to run this test. This method utilizes the
   * state information set in the setup method to run the signature tests. All
   * signature test code resides in the utility class so it can be reused by the
   * signature test framework base classes.
   *
   * @throws Fault
   *           When an error occurs executing the signature tests.
   */
  public void signatureTest() throws Fault {
    TestUtil.logMsg("$$$ SigTestEE.signatureTest() called");
    SigTestResult results = null;
    String mapFile = getMapFile();
    String repositoryDir = getRepositoryDir();
    String[] packages = getPackages(testInfo.getVehicle());
    String[] classes = getClasses(testInfo.getVehicle());
    String packageFile = getPackageFile();
    String testClasspath = testInfo.getTestClasspath();
    String optionalPkgToIgnore = testInfo.getOptionalTechPackagesToIgnore();

    // unlisted optional packages are technology packages for those optional
    // technologies (e.g. jsr-88) that might not have been specified by the
    // user.
    // We want to ensure there are no full or partial implementations of an
    // optional technology which were not declared
    ArrayList<String> unlistedTechnologyPkgs = getUnlistedOptionalPackages();

    // If testing with Java 9+, extract the JDK's modules so they can be used
    // on the testcase's classpath.
    Properties sysProps = System.getProperties();
    String version = (String) sysProps.get("java.version");
    if (!version.startsWith("1.")) {
      String jimageDir = testInfo.getJImageDir();
      File f = new File(jimageDir);
      f.mkdirs();

      String javaHome = (String) sysProps.get("java.home");
      TestUtil.logMsg("Executing JImage");

      try {
        ProcessBuilder pb = new ProcessBuilder(javaHome + "/bin/jimage", "extract", "--dir=" + jimageDir, javaHome + "/lib/modules");
        TestUtil.logMsg(javaHome + "/bin/jimage extract --dir=" + jimageDir + " " + javaHome + "/lib/modules");
        pb.redirectErrorStream(true);
        Process proc = pb.start();
        BufferedReader out = new BufferedReader(new InputStreamReader(proc.getInputStream()));
        String line = null;
        while ((line = out.readLine()) != null) {
          TestUtil.logMsg(line);
        }

        int rc = proc.waitFor();
        TestUtil.logMsg("JImage RC = " + rc);
        out.close();
      } catch (Exception e) {
        TestUtil.logMsg("Exception while executing JImage!  Some tests may fail.");
        e.printStackTrace();
      }
    }

    try {
      results = getSigTestDriver().executeSigTest(packageFile, mapFile,
          repositoryDir, packages, classes, testClasspath,
          unlistedTechnologyPkgs, optionalPkgToIgnore);
      TestUtil.logMsg(results.toString());
      if (!results.passed()) {
        TestUtil.logErr("results.passed() returned false");
        throw new Exception();
      }

      // Call verifyJtaJarTest based on some conditions, please check the
      // comment for verifyJtaJarTest.
      if ("standalone".equalsIgnoreCase(testInfo.getVehicle())) {
        Properties mapFileAsProps = getSigTestDriver().loadMapFile(mapFile);
        if (mapFileAsProps == null || mapFileAsProps.size() == 0) {
          // empty signature file, something unusual
          TestUtil.logMsg("SigTestEE.signatureTest() returning, " +
              "as signature map file is empty.");
          return;
        }

        boolean isJTASigTest = false;

        // Determine whether the signature map file contains package 
        // jakarta.transaction
        String jtaVersion = mapFileAsProps.getProperty("jakarta.transaction");
        if (jtaVersion == null || "".equals(jtaVersion.trim())) {
          TestUtil.logMsg("SigTestEE.signatureTest() returning, " +
              "as this is neither JTA TCK run, not Java EE CTS run.");
          return;
        }

        TestUtil.logMsg("jtaVersion " + jtaVersion);  
        // Signature map packaged in JTA TCK will contain a single package 
        // jakarta.transaction
        if (mapFileAsProps.size() == 1) {
            isJTASigTest = true;
        }

        if (isJTASigTest || !jtaVersion.startsWith("1.2")) {
          verifyJtaJarTest();
        }
      }
      TestUtil.logMsg("$$$ SigTestEE.signatureTest() returning");
    } catch (Exception e) {
      if (results != null && !results.passed()) {
        throw new Fault("SigTestEE.signatureTest() failed!, diffs found");
      } else {
        TestUtil.logErr("Unexpected exception " + e.getMessage());
        throw new Fault("signatureTest failed with an unexpected exception", e);
      }
    }
  }

  /**
   * Called by the test framework to run this test.  This method utilizes the
   * state information set in the setup method to run.  This test validates
   * that the javax.transaction.xa type is not in the JTA API jar
   *
   * This method is called only for standaone vehicle, as calling the same
   * for all the vehicles in the CTS run is not necessary.
   *
   * This method is called always from JTA 1.3 TCK. The test will be run as
   * part of Java EE Signature Test only when the signature map in the CTS
   * bundle is using JTA 1.3 (or higher) signature file.
   *
   * If property ts.jte jtaJarClasspath is removed in ts.jte of the JTA 1.3 TCK,
   * this test will display the available options to call SignatureTest and
   * fail. Similar failure will be seen in CTS run, if the signature map points
   * to JTA 1.3 signature file and the property jtaJarClasspath is removed from
   * ts.jte of CTS bundle.
   *
   * @throws Fault When an error occurs executing the signature tests.
   */
  public void verifyJtaJarTest() throws Exception {
    TestUtil.logMsg("SigTestEE#verifyJtaJarTest - Starting:");
    String repositoryDir = getRepositoryDir();
    String jtaJarClasspath = testInfo.getJtaJarClasspath();
    boolean result = getSigTestDriver().verifyJTAJarForNoXA(
                testInfo.getJtaJarClasspath(), repositoryDir);
    if(result) {
      TestUtil.logMsg("PASS: javax.transaction.xa not found in API jar");
    } else {
      TestUtil.logErr("FAIL: javax.transaction.xa found in API jar");
      throw new Fault("javax.transaction.xa validation failed");
    }
    TestUtil.logMsg("SigTestEE#verifyJtaJarTest returning");
  }

  /**
   * Called by the test framework to cleanup any outstanding state. This method
   * simply passes the message through to the utility class so the
   * implementation can be used by both framework base classes.
   *
   * @throws Fault
   *           When an error occurs cleaning up the state of this test.
   */
  public void cleanup() throws Fault {
    TestUtil.logMsg("$$$ SigTestEE.cleanup() called");
    try {
      getSigTestDriver().cleanupImpl();
      TestUtil.logMsg("$$$ SigTestEE.cleanup() returning");
    } catch (Exception e) {
      throw new Fault("Cleanup failed!", e);
    }
  }


  public static class Fault extends Exception {
    private static final long serialVersionUID = -1574745208867827913L;

    public Throwable t;

    /**
     * creates a Fault with a message
     */
    public Fault(String msg) {
      super(msg);
      TestUtil.logErr(msg);
    }

    /**
     * creates a Fault with a message.
     *
     * @param msg
     *          the message
     * @param t
     *          prints this exception's stacktrace
     */
    public Fault(String msg, Throwable t) {
      super(msg);
      this.t = t;
      TestUtil.logErr(msg, t);
    }

    /**
     * creates a Fault with a Throwable.
     *
     * @param t
     *          the Throwable
     */
    public Fault(Throwable t) {
      super(t);
      this.t = t;
    }

    /**
     * Prints this Throwable and its backtrace to the standard error stream.
     *
     */
    public void printStackTrace() {
      if (this.t != null) {
        this.t.printStackTrace();
      } else {
        super.printStackTrace();
      }
    }

    /**
     * Prints this throwable and its backtrace to the specified print stream.
     *
     * @param s
     *          <code>PrintStream</code> to use for output
     */
    public void printStackTrace(PrintStream s) {
      if (this.t != null) {
        this.t.printStackTrace(s);
      } else {
        super.printStackTrace(s);
      }
    }

    /**
     * Prints this throwable and its backtrace to the specified print writer.
     *
     * @param s
     *          <code>PrintWriter</code> to use for output
     */
    public void printStackTrace(PrintWriter s) {
      if (this.t != null) {
        this.t.printStackTrace(s);
      } else {
        super.printStackTrace(s);
      }
    }

    @Override
    public Throwable getCause() {
      return t;
    }

    @Override
    public synchronized Throwable initCause(Throwable cause) {
      if (t != null)
        throw new IllegalStateException("Can't overwrite cause");
      if (!Exception.class.isInstance(cause))
        throw new IllegalArgumentException("Cause not permitted");
      this.t = (Exception) cause;
      return this;
    }
  }

} // end class SigTestEE
