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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SigTestResult implements Serializable {

  private static final String NL = System.getProperty("line.separator", "\n");

  private List failedPkgs = new ArrayList();

  private List passedPkgs = new ArrayList();

  private List failedClasses = new ArrayList();

  private List passedClasses = new ArrayList();

  // ---------------------------------------------------------- Public Methods

  public synchronized boolean passed() {

    return (failedPkgs.size() == 0 && failedClasses.size() == 0);

  } // end passed

  public synchronized void addFailedPkg(String pkg) {

    failedPkgs.add(pkg);

  } // END addFailedPkg

  public synchronized void addPassedPkg(String pkg) {

    passedPkgs.add(pkg);

  } // END addPassedPkg

  public synchronized void addFailedClass(String className) {

    failedClasses.add(className);

  } // END addFailedClass

  public synchronized void addPassedClass(String className) {

    passedClasses.add(className);

  } // END addPassedClass

  public String toString() {

    String delim = "******************************************************"
        + NL;
    if (!pkgsTested() && !classesTested()) {
      return (delim + "******** No packages or classes were tested **********"
          + NL + delim);
    }
    StringBuffer buf = new StringBuffer();
    buf.append(delim);
    buf.append(delim);
    if (passed()) {
      buf.append("All package signatures passed.").append(NL);
    } else {
      buf.append("Some signatures failed.").append(NL);
      if (failedPkgs.size() > 0) {
        buf.append("\tFailed packages listed below: ").append(NL);
        formatList(failedPkgs, buf);
      }
      if (failedClasses.size() > 0) {
        buf.append("\tFailed classes listed below: ").append(NL);
        formatList(failedClasses, buf);
      }
    }
    if (passedPkgs.size() > 0) {
      buf.append("\tPassed packages listed below: ").append(NL);
      formatList(passedPkgs, buf);
    }
    if (passedClasses.size() > 0) {
      buf.append("\tPassed classes listed below: ").append(NL);
      formatList(passedClasses, buf);
    }
    buf.append("\t");
    buf.append(delim);
    buf.append(delim);
    return buf.toString();

  } // END toString

  // --------------------------------------------------------- Private Methods

  private synchronized void formatList(List list, StringBuffer buf) {

    synchronized (this) {
      for (int i = 0; i < list.size(); i++) {
        String pkg = (String) (list.get(i));
        buf.append("\t\t").append(pkg).append(NL);
      }
    }

  } // END formatList

  private synchronized boolean pkgsTested() {

    return (failedPkgs.size() != 0 || passedPkgs.size() != 0);

  } // END pkgsTested

  private synchronized boolean classesTested() {

    return (failedClasses.size() != 0 || passedClasses.size() != 0);

  } // END classesTested

} // end class SigTestResult
