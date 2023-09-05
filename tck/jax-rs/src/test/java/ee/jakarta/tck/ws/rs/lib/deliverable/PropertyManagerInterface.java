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

package ee.jakarta.tck.ws.rs.lib.deliverable;

import java.util.Properties;

/**
 * This class serves as a well known place for harness, util, and porting
 * classes to retrieve property values.
 *
 * @author Kyle Grucci
 */
public interface PropertyManagerInterface {

  /**
   * This method swaps all of the following interop values in
   * TSPropertyManager...
   *
   */
  public void swapInteropPropertyValues(String sDirection);

  /**
   * gets a new properties containing all entries in the property manager. Any
   * operation on the returned properties will have no effect on property
   * manager
   */
  public Properties getJteProperties();

  /**
   * gets property value with default
   *
   * @param sKey - Property to retrieve
   * @param default - default value to use
   * @return String - property value
   */
  public String getProperty(String sKey, String def);

  /**
   * This method is called to get a property value
   *
   * @param sKey
   *          - Property to retrieve
   * @return String - property value
   */
  public String getProperty(String sKey) throws PropertyNotSetException;

  /**
   * This method is called to set a property on the property manager
   *
   * @param skey - key to be used
   * @param sVal - value to use
   */
  public void setProperty(String sKey, String sVal);

  /**
   * This method is called by the test harness to retrieve all properties needed
   * by a particular test.
   *
   * @param sPropKeys - Properties to retrieve
   * @return Properties - property/value pairs
   */
  public Properties getTestSpecificProperties(String[] sPropKeys)
      throws PropertyNotSetException;
}
