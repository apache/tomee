/*
 * Copyright (c) 2017, 2020 Oracle and/or its affiliates. All rights reserved.
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

package ee.jakarta.tck.ws.rs.jaxrs21.api.client.invocationbuilder;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.RxInvoker;
import jakarta.ws.rs.core.GenericType;

public class TCKRxInvoker implements RxInvoker<CompletionStage<String>> {

  private static final String RESULT = "Some string that to initialize the completable future";

  private CompletableFuture<String> future = new CompletableFuture<>();

  {
    future.complete(RESULT);
  }

  @Override
  public CompletionStage<String> get() {
    return future;
  }

  @Override
  public <R> CompletionStage<String> get(Class<R> responseType) {
    return future;
  }

  @Override
  public <R> CompletionStage<String> get(GenericType<R> responseType) {
    return future;
  }

  @Override
  public CompletionStage<String> put(Entity<?> entity) {
    return future;
  }

  @Override
  public <R> CompletionStage<String> put(Entity<?> entity,
      Class<R> responseType) {
    return future;
  }

  @Override
  public <R> CompletionStage<String> put(Entity<?> entity,
      GenericType<R> responseType) {
    return future;
  }

  @Override
  public CompletionStage<String> post(Entity<?> entity) {
    return future;
  }

  @Override
  public <R> CompletionStage<String> post(Entity<?> entity,
      Class<R> responseType) {
    return future;
  }

  @Override
  public <R> CompletionStage<String> post(Entity<?> entity,
      GenericType<R> responseType) {
    return future;
  }

  @Override
  public CompletionStage<String> delete() {
    return future;
  }

  @Override
  public <R> CompletionStage<String> delete(Class<R> responseType) {
    return future;
  }

  @Override
  public <R> CompletionStage<String> delete(GenericType<R> responseType) {
    return future;
  }

  @Override
  public CompletionStage<String> head() {
    return future;
  }

  @Override
  public CompletionStage<String> options() {
    return future;
  }

  @Override
  public <R> CompletionStage<String> options(Class<R> responseType) {
    return future;
  }

  @Override
  public <R> CompletionStage<String> options(GenericType<R> responseType) {
    return future;
  }

  @Override
  public CompletionStage<String> trace() {
    return future;
  }

  @Override
  public <R> CompletionStage<String> trace(Class<R> responseType) {
    return future;
  }

  @Override
  public <R> CompletionStage<String> trace(GenericType<R> responseType) {
    return future;
  }

  @Override
  public CompletionStage<String> method(String name) {
    return future;
  }

  @Override
  public <R> CompletionStage<String> method(String name,
      Class<R> responseType) {
    return future;
  }

  @Override
  public <R> CompletionStage<String> method(String name,
      GenericType<R> responseType) {
    return future;
  }

  @Override
  public CompletionStage<String> method(String name, Entity<?> entity) {
    return future;
  }

  @Override
  public <R> CompletionStage<String> method(String name, Entity<?> entity,
      Class<R> responseType) {
    return future;
  }

  @Override
  public <R> CompletionStage<String> method(String name, Entity<?> entity,
      GenericType<R> responseType) {
    return future;
  }
}
