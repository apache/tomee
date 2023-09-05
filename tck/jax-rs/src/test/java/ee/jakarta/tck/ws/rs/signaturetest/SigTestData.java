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
 * $Id$
 */

package ee.jakarta.tck.ws.rs.signaturetest;

import java.util.Properties;

/**
 * This class holds the data passed to a signature test invocation during the
 * setup phase. This allows us to keep the passed data separate and reuse the
 * data between the signature test framework base classes.
 */
public class SigTestData {

  private Properties props;

  public SigTestData() {
    this.props = System.getProperties();;
  }

  public String getVehicle() {
    return props.getProperty("vehicle", "");
  }

  public String getBinDir() {
    return props.getProperty("bin.dir", "");
  }

  public String getTSHome() {
    return props.getProperty("ts_home", "");
  }

  public String getTestClasspath() {
    return props.getProperty("sigTestClasspath", "");
  }

  public String getJavaeeLevel() {
    return props.getProperty("javaee.level", "");
  }

  public String getCurrentKeywords() {
    return props.getProperty("current.keywords", "");
  }

  public String getProperty(String prop) {
    return props.getProperty(prop);
  }

  public String getOptionalTechPackagesToIgnore() {
    return props.getProperty("optional.tech.packages.to.ignore", "jakarta.xml.bind");
  }

  public String getJtaJarClasspath() {
    return props.getProperty("jtaJarClasspath", "");
  }

  public String getJImageDir() {
    return props.getProperty("jimage.dir", "");
  }
} // end class SigTestData
