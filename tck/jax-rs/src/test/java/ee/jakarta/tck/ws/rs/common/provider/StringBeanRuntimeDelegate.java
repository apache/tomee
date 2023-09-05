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

 package ee.jakarta.tck.ws.rs.common.provider;

import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Link;
import jakarta.ws.rs.core.EntityPart;
import jakarta.ws.rs.core.Response.ResponseBuilder;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.Variant.VariantListBuilder;
import jakarta.ws.rs.ext.RuntimeDelegate;

import java.util.concurrent.CompletionStage;

import jakarta.ws.rs.SeBootstrap;
import jakarta.ws.rs.SeBootstrap.Instance;

public class StringBeanRuntimeDelegate extends RuntimeDelegate {

  public StringBeanRuntimeDelegate(RuntimeDelegate orig) {
    super();
    this.original = orig;
    assertNotStringBeanRuntimeDelegate(orig);
  }

  private RuntimeDelegate original;

  @Override
  public <T> T createEndpoint(Application arg0, Class<T> arg1)
      throws IllegalArgumentException, UnsupportedOperationException {
    return original.createEndpoint(arg0, arg1);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> HeaderDelegate<T> createHeaderDelegate(Class<T> arg0)
      throws IllegalArgumentException {
    if (arg0 == StringBean.class)
      return (HeaderDelegate<T>) new StringBeanHeaderDelegate();
    else
      return original.createHeaderDelegate(arg0);
  }

  @Override
  public ResponseBuilder createResponseBuilder() {
    return original.createResponseBuilder();
  }

  @Override
  public UriBuilder createUriBuilder() {
    return original.createUriBuilder();
  }

  @Override
  public VariantListBuilder createVariantListBuilder() {
    return original.createVariantListBuilder();
  }

  @Override
  public EntityPart.Builder createEntityPartBuilder(String partName) {
    return original.createEntityPartBuilder(partName);
  }

  @Override
  public CompletionStage<Instance> bootstrap(Application application, SeBootstrap.Configuration configuration) {
    return original.bootstrap(application, configuration);
  }

  @Override
  public CompletionStage<Instance> bootstrap(Class<? extends Application> applicationClass, SeBootstrap.Configuration configuration) {
    return original.bootstrap(applicationClass, configuration);
  }

  @Override
  public SeBootstrap.Configuration.Builder createConfigurationBuilder() {
    return original.createConfigurationBuilder();
  }

  public RuntimeDelegate getOriginal() {
    return original;
  }

  public static final void assertNotStringBeanRuntimeDelegate() {
    RuntimeDelegate delegate = RuntimeDelegate.getInstance();
    assertNotStringBeanRuntimeDelegate(delegate);
  }

  public static final//
  void assertNotStringBeanRuntimeDelegate(RuntimeDelegate delegate) {
    if (delegate instanceof StringBeanRuntimeDelegate) {
      StringBeanRuntimeDelegate sbrd = (StringBeanRuntimeDelegate) delegate;
      if (sbrd.getOriginal() != null)
        RuntimeDelegate.setInstance(sbrd.getOriginal());
      throw new RuntimeException(
          "RuntimeDelegate.getInstance() is StringBeanRuntimeDelegate");
    }
  }

  @Override
  public Link.Builder createLinkBuilder() {
    return original.createLinkBuilder();
  }
}
