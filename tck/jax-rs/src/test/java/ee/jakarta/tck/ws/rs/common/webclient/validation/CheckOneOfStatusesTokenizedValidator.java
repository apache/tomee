/*
 * Copyright (c) 2012, 2021 Oracle and/or its affiliates. All rights reserved.
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

package ee.jakarta.tck.ws.rs.common.webclient.validation;

import java.io.IOException;

/**
 * Sometimes it is not clear what result one should get, there might be more two
 * or more possibilities. This strategy checks the response contains at least
 * one of the given statuses.
 * 
 * The statuses are supposed to be separated by "|" character
 * 
 * @author Jan Supol
 */
public class CheckOneOfStatusesTokenizedValidator extends TokenizedValidator {

  /**
   * When WebTestCase contains more expected response codes it always means to
   * check one of them is present; if present, other statuses are dropped. Super
   * class method is called to get the logging messages
   */
  @Override
  protected boolean checkStatusCode() throws IOException {
    String responseCode = _res.getStatusCode();
    String caseCodes = _case.getStatusCode();

    if (caseCodes != null && caseCodes.charAt(0) != '!'
        && caseCodes.contains("|") && caseCodes.contains(responseCode))
      _case.setExpectedStatusCode(responseCode);
    return super.checkStatusCode();
  }

}
