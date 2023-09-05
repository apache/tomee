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

package ee.jakarta.tck.ws.rs.spec.resource.requestmatching;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("yas")
public class YetAnotherSubresource {
  @GET
  @Produces("text/*")
  public String getTextStar() {
    return "text/*";
  }

  @POST
  @Consumes("text/*")
  public String postTextStar() {
    return "text/*";
  }

  @POST
  @Consumes("text/xml;qs=0.7")
  public String xml() {
    return MediaType.TEXT_XML;
  }

  @GET
  @Produces("text/xml;qs=0.7")
  public String xmlGet() {
    return MediaType.TEXT_XML;
  }

  @GET
  @Produces("application/xml;qs=0.8")
  public String appXmlGet() {
    return MediaType.APPLICATION_XML;
  }

  @GET
  @Produces("testiii/textiii;qs=0.7")
  public String testiiiTextiiiGet() {
    return "testiii/textiii";
  }

  @GET
  @Produces("testi/*")
  public String testStar() {
    return "test/*";
  }

  @GET
  @Produces("testi/text")
  public String testText() {
    return "test/text";
  }

  @GET
  @Produces("testii/texta")
  public String testIITextA() {
    return "textA";
  }

  @GET
  @Produces("testii/textb")
  public String testIITextB() {
    return "textB";
  }

}
