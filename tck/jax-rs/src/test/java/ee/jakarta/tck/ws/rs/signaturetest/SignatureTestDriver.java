/*
 * Copyright (c) 2007, 2021 Oracle and/or its affiliates and others.
 * All rights reserved.
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Properties;

import ee.jakarta.tck.ws.rs.lib.util.TestUtil;

/**
 * Allows the sigtest framework to be extended using different signature test
 * implementations (e.g. ApiCheck, or SigTest)
 */
public abstract class SignatureTestDriver {

  private static final String SIG_FILE_EXT = ".sig";

  private static final String SIG_FILE_VER_SEP = "_";

  // ---------------------------------------------------------- Public Methods

  /**
   * Implementation of the getPackageFile method defined in both the SigTest and
   * SigTestEE class.
   */
  public String getPackageFileImpl(String binDir) {

    String thePkgListFile = "sig-test-pkg-list.txt";

    TestUtil.logMsg(
        "Using the following as the SigTest Package file: " + thePkgListFile);

    String theFile = binDir + File.separator + thePkgListFile;
    File ff = new File(theFile);
    if (!ff.exists()) {
      // we could not find the map file that coresponded to our SE version so
      // lets
      // try to default to use the sig-test-pkg-list.txt
      TestUtil
          .logErr("The SigTest Package file does not exist: " + thePkgListFile);
      theFile = binDir + File.separator + "sig-test-pkg-list.txt";
      File ff2 = new File(theFile);
      if (!ff2.exists()) {
        TestUtil
            .logErr("The Default SigTest Package file does not exist either: "
                + theFile);
      } else {
        TestUtil.logMsg("Defaulting to using SigTest Package file: " + theFile);
      }
    }

    return (theFile);

  } // END getPackageFileImpl

  /**
   * Implementation of the getMapFile method defined in both the SigTest and
   * SigTestEE class.
   */
  public String getMapFileImpl(String binDir) {

    String  theMapFile = "sig-test.map";

    TestUtil
        .logMsg("Using the following as the sig-Test map file: " + theMapFile);

    String theFile = binDir + File.separator + theMapFile;
    File ff = new File(theFile);
    if (!ff.exists()) {
      // we could not find the map file that coresponded to our SE version so
      // lets
      // try to default to use the sig-test.map
      TestUtil.logErr("The SigTest Map file does not exist: " + theMapFile);
      theFile = binDir + File.separator + "sig-test.map";
      File ff2 = new File(theFile);
      if (!ff2.exists()) {
        TestUtil
            .logErr("The SigTest Map file does not exist either: " + theFile);
      } else {
        TestUtil.logMsg("Defaulting to using SigTest Map file: " + theFile);
      }
    }

    return (theFile);

  } // END getMapFileImpl

  /**
   * Returns true if the passed in version matches the current Java version
   * being used.
   * 
   */
  public Boolean isJavaSEVersion(String ver) {

    String strOSVersion = System.getProperty("java.version");
    if (strOSVersion.startsWith(ver)) {
      return true;
    } else {
      return false;
    }
  }

  /**
   * Implementation of the getRepositoryDir method defined in both the SigTest
   * and SigTestEE class.
   */
  public String getRepositoryDirImpl(String tsHome) {

    return (tsHome + File.separator + "src" + File.separator + "com"
        + File.separator + "sun" + File.separator + "ts" + File.separator
        + "tests" + File.separator + "signaturetest" + File.separator
        + "signature-repository" + File.separator);

  } // END getRepositoryDirImpl

  /**
   * Implementation of the cleanup method defined in both the SigTest and
   * SigTestEE class.
   */
  public void cleanupImpl() throws Exception {

    try {
      TestUtil.logMsg("cleanup");
    } catch (Exception e) {
      TestUtil.logErr("Exception in cleanup method" + e);
      throw e;
    }

  } // END cleanupImpl

  /**
   * <p>
   * Execute the signature test. By default, this method passes the result of
   * {@link #createTestArguments(String, String, String, String, String)} and
   * passes the result to {@link #runSignatureTest(String, String[])}.
   *
   * @param packageListFile
   *          - file containing the packages/classes that are to be verified
   * @param mapFile
   *          sig-test.map file
   * @param signatureRepositoryDir
   *          directory containing the recorded signatures
   * @param packagesUnderTest
   *          packages, defined by the test client, that should be tested
   * @param classesUnderTest
   *          classes, defined by the test client, that should be tested
   * @param classpath
   *          The location of the API being verified. Normally the checked API
   *          will be available in the test environment and testClasspath will
   *          be null. In some rare cases the tested API may not be part of the
   *          test environment and will have to specified using this parameter.
   * @param unaccountedTechPkgs
   *          packages that should not exist within the technology under test.
   *          These will be searched for and if found, will be flagged as error
   *          since they were not explicitly declared as being under test. Their
   *          existence requires explicit testing.
   *
   * @return a {@link SigTestResult} containing the result of the test execution
   */
  public SigTestResult executeSigTest(String packageListFile, String mapFile,
      String signatureRepositoryDir, String[] packagesUnderTest,
      String[] classesUnderTest, String classpath,
      ArrayList<String> unaccountedTechPkgs, String optionalPkgToIgnore)
      throws Exception {

    SigTestResult result = new SigTestResult();

    TestUtil.logMsg("optionalPkgToIgnore = " + optionalPkgToIgnore);
    String[] arrayOptionalPkgsToIgnore = null;
    if (optionalPkgToIgnore != null) {
      arrayOptionalPkgsToIgnore = optionalPkgToIgnore.split(",");
    }

    if (packagesUnderTest != null && packagesUnderTest.length > 0) {
      TestUtil.logMsg("********** BEGIN PACKAGE LEVEL SIGNATURE "
          + "VALIDATION **********\n\n");
      for (int i = 0; i < packagesUnderTest.length; i++) {

        String packageName = packagesUnderTest[i];

        TestUtil.logMsg("********** BEGIN VALIDATE PACKAGE '"
            + packagesUnderTest[i] + "' **********\n");

        TestUtil.logMsg(
            "********** VALIDATE IN STATIC MODE - TO CHECK CONSANT VALUES ****");
        TestUtil
            .logMsg("Static mode supports checks of static constants values ");

        String[] args = createTestArguments(packageListFile, mapFile,
            signatureRepositoryDir, packageName, classpath, true);
        dumpTestArguments(args);

        if (runSignatureTest(packageName, args)) {
          TestUtil.logMsg("********** Package '" + packageName
              + "' - PASSED (STATIC MODE) **********");
          result.addPassedPkg(packageName + "(static mode)");
        } else {
          result.addFailedPkg(packageName + "(static mode)");
          TestUtil.logMsg("********** Package '" + packageName
              + "' - FAILED (STATIC MODE) **********");
        }

        TestUtil.logMsg("\n\n");
        TestUtil.logMsg("********** VALIDATE IN REFLECTIVE MODE  ****");
        TestUtil.logMsg(
            "Reflective mode supports verification within containers (ie ejb, servlet, etc)");

        String[] args2 = createTestArguments(packageListFile, mapFile,
            signatureRepositoryDir, packageName, classpath, false);
        dumpTestArguments(args2);

        if (runSignatureTest(packageName, args2)) {
          TestUtil.logMsg("********** Package '" + packageName
              + "' - PASSED (REFLECTION MODE) **********");
          result.addPassedPkg(packageName + "(reflection mode)");
        } else {
          result.addFailedPkg(packageName + "(reflection mode)");
          TestUtil.logMsg("********** Package '" + packageName
              + "' - FAILED (REFLECTION MODE) **********");
        }

        TestUtil.logMsg("********** END VALIDATE PACKAGE '"
            + packagesUnderTest[i] + "' **********\n");

        TestUtil.logMsg("\n");
        TestUtil.logMsg("\n");

      }
    }

    if (classesUnderTest != null && classesUnderTest.length > 0) {
      TestUtil.logMsg("********** BEGIN CLASS LEVEL SIGNATURE "
          + "VALIDATION **********\n\n");

      for (int i = 0; i < classesUnderTest.length; i++) {

        String className = classesUnderTest[i];

        TestUtil.logMsg("********** BEGIN VALIDATE CLASS '"
            + classesUnderTest[i] + "' **********\n");

        TestUtil.logMsg(
            "********** VALIDATE IN STATIC MODE - TO CHECK CONSANT VALUES ****");
        TestUtil
            .logMsg("Static mode supports checks of static constants values ");

        String[] args = createTestArguments(packageListFile, mapFile,
            signatureRepositoryDir, className, classpath, true);
        dumpTestArguments(args);

        if (runSignatureTest(className, args)) {
          TestUtil.logMsg("********** Class '" + className
              + "' - PASSED (STATIC MODE) **********");
          result.addPassedClass(className + "(static mode)");
        } else {
          TestUtil.logMsg("********** Class '" + className
              + "' - FAILED (STATIC MODE) **********");
          result.addFailedClass(className + "(static mode)");
        }

        TestUtil.logMsg("\n\n");
        TestUtil.logMsg("********** VALIDATE IN REFLECTIVE MODE  ****");
        TestUtil.logMsg(
            "Reflective mode supports verification within containers (ie ejb, servlet, etc)");

        String[] args2 = createTestArguments(packageListFile, mapFile,
            signatureRepositoryDir, className, classpath, false);
        dumpTestArguments(args2);

        if (runSignatureTest(className, args2)) {
          TestUtil.logMsg("********** Class '" + className
              + "' - PASSED (REFLECTION MODE) **********");
          result.addPassedClass(className + "(reflection mode)");
        } else {
          TestUtil.logMsg("********** Class '" + className
              + "' - FAILED (REFLECTION MODE) **********");
          result.addFailedClass(className + "(reflection mode)");
        }

        TestUtil.logMsg("********** END VALIDATE CLASS '" + classesUnderTest[i]
            + "' **********\n");

        TestUtil.logMsg("\n");
        TestUtil.logMsg("\n");

      }
    }

    /*
     * The following will check if there are Optional Technologies being
     * implemented but not explicitly defined thru (ts.jte) javaee.level
     * property. This is a problem because if an optional technolgy is defined
     * (either whole or partially) than the TCK tests (and sig tests) for those
     * Optional Technology(s) MUST be run according to related specs.
     */
    if (unaccountedTechPkgs != null) {
      for (int ii = 0; ii < unaccountedTechPkgs.size(); ii++) {
        // 'unaccountedTechPkgs' are t hose packages which do not beling to
        // base technology nor one of the *declared* optionalal technologies.
        // 'unaccountedTechPkgs' refers to packages for Optional Technologies
        // which were not defined thru (ts.jte) javaee.level property.
        // So, make sure there are no whole or partial implementations of
        // undeclared optional technologies in the implementation

        String packageName = unaccountedTechPkgs.get(ii);

        // this is a special case exception to our validation of Optional
        // Technologies. Normally any partial technology implementations
        // would be a compatibility failure. HOWEVER, EE 7 Spec (see section
        // EE 6.1.2 of the Platform spec in the footnote on p. 156.)
        // requires us to add special handling to avoid testing 'certain' pkgs
        // within an optional technology.
        if (isIgnorePackageUnderTest(packageName, arrayOptionalPkgsToIgnore)) {
          TestUtil.logMsg(
              "Ignoring special optional technology package: " + packageName);
          continue;
        }

        TestUtil.logMsg("\n\n");
        TestUtil.logMsg(
            "********** CHECK IF OPTIONAL TECHNOLOGIES EXIST IN REFLECTIVE MODE  ****");
        TestUtil.logMsg(
            "Reflective mode supports verification within containers (ie ejb, servlet, etc)");

        String[] args3 = createTestArguments(packageListFile, mapFile,
            signatureRepositoryDir, packageName, classpath, false);
        dumpTestArguments(args3);

        // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
        // - - - -
        // NOTE: this is the opposite of above in that *if* we find that an
        // undeclared
        // optional technology package exists - then we want to raise a red
        // flag.
        // The user would have to either remove the technology from the impl if
        // they do not want to include it in their impl -OR- they must
        // explicitly
        // set javaee.level (in ts.jte) to include that Optional Technology AND
        // after setting this property, they have to pass all related TCK tests.
        if (runPackageSearch(packageName, args3)) {
          // if this passed we have an issue because it should not exist - thus
          // should NOT pass.
          TestUtil.logMsg("********** Package '" + packageName
              + "' - WAS FOUND BUT SHOULD NOT BE (REFLECTION MODE) **********");
          String err = "ERROR:  An area of concern has been identified.  ";
          err += "You must run sigtests with (ts.jte) javaee.level set to ";
          err += "include all optional technology keywords.  Whole and/or ";
          err += "partial implementations of Optional Technologies ";
          err += "must be implemented according to the specs AND must pass ";
          err += "all related TCK tests.  To properly pass the ";
          err += "signature tests - you must identify all Optional Technology ";
          err += "areas (via javaee.level) that you wish to pass signature tests for.";
          TestUtil.logMsg(err);
          result.addFailedPkg(packageName
              + " (Undeclared Optional Technology package found in reflection mode)");
        } else {
          TestUtil.logMsg("********** Undeclared Optional Technology package '"
              + packageName + "' - PASSED (REFLECTION MODE) **********");
        }
      }
    }

    return result;

  } // END executeSigTest

  // ------------------------------------------------------- Protected Methods

  /**
   * Using a common set of information, create arguments that are appropriate to
   * be used with the underlying signature test framework.
   *
   * @param packageListFile
   *          - file containing the packages/classes that are to be verified
   * @param mapFile
   *          sig-test.map file
   * @param signatureRepositoryDir
   *          directory containing the recorded signatures
   * @param packageOrClassUnderTest
   *          the class or package
   * @param classpath
   *          The location of the API being verified. Normally the checked API
   *          will be available in the test environment and testClasspath will
   *          be null. In some rare cases the tested API may not be part of the
   *          test environment and will have to specified using this parameter.
   */
  protected abstract String[] createTestArguments(String packageListFile,
      String mapFile, String signatureRepositoryDir,
      String packageOrClassUnderTest, String classpath, boolean bStaticMode)
      throws Exception;

  /**
   * Invoke the underlying signature test framework for the specified package or
   * class.
   *
   * @param packageOrClassName
   *          the package or class to be validated
   * @param testArguments
   *          the arguments necessary to invoke the signature test framework
   *
   * @return <code>true</code> if the test passed, otherwise <code>false</code>
   */
  protected abstract boolean runSignatureTest(String packageOrClassName,
      String[] testArguments) throws Exception;

  /**
   * This checks if a class exists or not within the impl.
   *
   * @param packageOrClassName
   *          the package or class to be validated
   *
   * @return <code>true</code> if the package was found to exist, otherwise
   *         <code>false</code>
   */
  protected abstract boolean runPackageSearch(String packageOrClassName,
      String[] testArguments) throws Exception;


  /**
   * This method checks whether JTA API jar contains classes from
   * javax.transaction.xa package
   *
   * @param classpath
   *           the classpath, pointing JTA API jar
   * @param repositoryDir
   *           the directory containing an empty signature file
   *
   * @return <code>true</code> if the package javax.transaction.xa is not
   *        found in the JTA API jar, otherwise <code>false</code>
   */
   protected  abstract boolean verifyJTAJarForNoXA(String classpath,
            String repositoryDir) throws Exception;

  /**
   * Loads the specified file into a Properties object provided the specified
   * file exists and is a regular file. The call to new FileInputStream verifies
   * that the specfied file is a regular file and exists.
   *
   * @param mapFile
   *          the path and name of the map file to be loaded
   *
   * @return Properties The Properties object initialized with the contents of
   *         the specified file
   *
   * @throws java.io.IOException
   *           If the specified map file does not exist or is not a regular
   *           file, can also be thrown if there is an error creating an input
   *           stream from the specified file.
   */
  public Properties loadMapFile(String mapFile)
      throws IOException, FileNotFoundException {

    FileInputStream in = null;
    try {
      File map = new File(mapFile);
      Properties props = new Properties();
      in = new FileInputStream(map);
      props.load(in);
      return props;
    } finally {
      try {
        if (in != null) {
          in.close();
        }
      } catch (Throwable t) {
        // do nothing
      }
    }

  } // END loadMapFile

  /**
   * This method will attempt to build a fully-qualified filename in the format
   * of <code>respositoryDir</code> + </code>baseName</code> +
   * <code>.sig_</code> + </code>version</code>.
   *
   * @param baseName
   *          the base portion of the signature filename
   * @param repositoryDir
   *          the directory in which the signatures are stored
   * @param version
   *          the version of the signature file
   * @throws FileNotFoundException
   *           if the file cannot be validated as existing and is in fact a file
   * @return a valid, fully qualified filename, appropriate for the system the
   *         test is being run on
   */
  protected String getSigFileName(String baseName, String repositoryDir,
      String version) throws FileNotFoundException {

    String sigFile;
    if (repositoryDir.endsWith(File.separator)) {
      sigFile = repositoryDir + baseName + SIG_FILE_EXT + SIG_FILE_VER_SEP
          + version;
    } else {
      sigFile = repositoryDir + File.separator + baseName + SIG_FILE_EXT
          + SIG_FILE_VER_SEP + version;
    }

    File testFile = new File(sigFile);

    if (!testFile.exists() && !testFile.isFile()) {
      throw new FileNotFoundException(
          "Signature file \"" + sigFile + "\" does not exist.");
    }

    // we are actually requiring this normalizeFileName call to get
    // things working on Windows. Without this, if we just return the
    // testFile; we will fail on windows. (Solaris works either way)
    // IMPORTANT UPDATE!! (4/5/2011)
    // in sigtest 2.2: they stopped supporting the normalized version which
    // created a string filename =
    // "file://com/sun/ts/tests/signaturetest/foo.sig"
    // so now use file path and name only.
    // return normalizeFileName(testFile);
    return testFile.toString();

  } // END getSigFileName

  protected abstract String normalizeFileName(File f);

  /**
   * Returns the name and path to the signature file that contains the specified
   * package's signatures.
   *
   * @param packageName
   *          The package under test
   * @param mapFile
   *          The name of the file that maps package names to versions
   * @param repositoryDir
   *          The directory that conatisn all signature files
   *
   * @return String The path and name of the siganture file that contains the
   *         specified package's signatures
   *
   * @throws Exception
   *           if the determined signature file is not a regular file or does
   *           not exist
   */
  protected SignatureFileInfo getSigFileInfo(String packageName, String mapFile,
      String repositoryDir) throws Exception {

    String originalPackage = packageName;
    String name = null;
    String version = null;
    Properties props = loadMapFile(mapFile);

    while (true) {
      boolean packageFound = false;
      for (Enumeration<?> e = props.propertyNames(); e.hasMoreElements();) {
        name = (String) (e.nextElement());
        if (name.equals(packageName)) {
          version = props.getProperty(name);
          packageFound = true;
          break;
        } // end if
      } // end for

      if (packageFound) {
        break;
      }

      /*
       * If we get here we did not find a package name in the properties file
       * that matches the package name under test. So we look for a package name
       * in the properties file that could be the parent package for the package
       * under test. We do this by removing the specified packages last package
       * name section. So jakarta.ejb.spi would become jakarta.ejb
       */
      int index = packageName.lastIndexOf(".");
      if (index <= 0) {
        throw new Exception("Package \"" + originalPackage
            + "\" not specified in mapping file \"" + mapFile + "\".");
      }
      packageName = packageName.substring(0, index);
    } // end while

    /* Return the expected name of the signature file */

    return new SignatureFileInfo(getSigFileName(name, repositoryDir, version),
        version);

  } // END getSigFileInfo

  // --------------------------------------------------------- Private Methods

  /*
   * This returns true is the passed in packageName matches one of the packages
   * that are listed in the arrayOptionalPkgsToIgnore. arrayOptionalPkgsToIgnore
   * is ultimately defined in the ts.jte property
   * 'optional.tech.packages.to.ignore' If one of the entries in
   * arrayOptionalPkgsToIgnore matches the packageName then that means we return
   * TRUE to indicate we should ignore and NOT TEST that particular package.
   */
  private static boolean isIgnorePackageUnderTest(String packageName,
      String[] arrayOptionalPkgsToIgnore) {

    // if anything is null - consider no match
    if ((packageName == null) || (arrayOptionalPkgsToIgnore == null)) {
      return false;
    }

    for (int ii = 0; ii < arrayOptionalPkgsToIgnore.length; ii++) {
      if (packageName.equals(arrayOptionalPkgsToIgnore[ii])) {
        // we found a match -
        return true;
      }
    }

    return false;
  }

  /**
   * Prints the specified list of parameters to the message log. Used for
   * debugging purposes only.
   *
   * @param params
   *          The list of parameters to dump.
   */
  private static void dumpTestArguments(String[] params) {

    if (params != null && params.length > 0) {
      TestUtil
          .logTrace("----------------- BEGIN SIG PARAM DUMP -----------------");
      for (int i = 0; i < params.length; i++) {
        TestUtil.logTrace("   Param[" + i + "]: " + params[i]);
      }
      TestUtil
          .logTrace("------------------ END SIG PARAM DUMP ------------------");
    }

  } // END dumpTestArguments

  // ----------------------------------------------------------- Inner Classes

  /**
   * A simple data structure containing the fully qualified path to the
   * signature file as well as the version being tested.
   */
  protected static class SignatureFileInfo {

    private String file;

    private String version;

    // -------------------------------------------------------- Constructors

    public SignatureFileInfo(String file, String version) {

      if (file == null) {
        throw new IllegalArgumentException("'file' argument cannot be null");
      }

      if (version == null) {
        throw new IllegalArgumentException("'version' argument cannot be null");
      }

      this.file = file;
      this.version = version;

    } // END SignatureFileInfo

    // ------------------------------------------------------ Public Methods

    public String getFile() {

      return file;

    } // END getFileIncludingPath

    public String getVersion() {

      return version;

    } // END getVersion

  }

} // END SigTestDriver
