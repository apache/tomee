/*
 * Copyright (c) 2013, 2021 Oracle and/or its affiliates. All rights reserved.
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

package ee.jakarta.tck.ws.rs.api.rs.ext.runtimedelegate;

import java.util.concurrent.CompletionStage;

import jakarta.ws.rs.SeBootstrap;
import jakarta.ws.rs.SeBootstrap.Instance;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.EntityPart;
import jakarta.ws.rs.core.Link;
import jakarta.ws.rs.core.Response.ResponseBuilder;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.Variant.VariantListBuilder;
import jakarta.ws.rs.ext.RuntimeDelegate;
import jakarta.ws.rs.ext.RuntimeDelegate.HeaderDelegate;

public class TckRuntimeDelegate extends RuntimeDelegate {

  @Override
  public <T> T createEndpoint(Application application, Class<T> endpointType)
      throws IllegalArgumentException, UnsupportedOperationException {
    return null;
  }

  @Override
  public <T> HeaderDelegate<T> createHeaderDelegate(Class<T> type) {
    return null;
  }

  @Override
  public ResponseBuilder createResponseBuilder() {
    return null;
  }

  @Override
  public UriBuilder createUriBuilder() {
    return null;
  }

  @Override
  public VariantListBuilder createVariantListBuilder() {
    return null;
  }

  @Override
  public Link.Builder createLinkBuilder() {
    return null;
  }

  @Override
  public SeBootstrap.Configuration.Builder createConfigurationBuilder() {
    return null;
  }

  @Override
  public EntityPart.Builder createEntityPartBuilder(String partName) throws IllegalArgumentException {
    return null;
  }

  @Override
  public CompletionStage<Instance> bootstrap(Application application, SeBootstrap.Configuration configuration) {
    return null;
  }

  @Override
  public CompletionStage<Instance> bootstrap(Class<? extends Application> application, SeBootstrap.Configuration configuration) {
    return null;
  }

}
