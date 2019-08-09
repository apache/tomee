/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.superbiz.asyncservlet;

import javax.ejb.EJB;
import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@WebServlet(urlPatterns = "/*", asyncSupported = true)
public class CalcServlet extends HttpServlet {

	@EJB
	private CalcBean bean;

	private final ExecutorService executorService = Executors.newFixedThreadPool(10);

	private static final List<String> OPERATIONS = Arrays.asList("ADD", "SUBTRACT", "MULTIPLY", "DIVIDE");

	@Override
	protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
		process(req, resp);
	}


	@Override
	protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
		super.doPost(req, resp);
	}

	private void process(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
		final String operation = req.getParameter("op");
		final String asyncParam = req.getParameter("async");
		final String delayParam = req.getParameter("delay");
		final String timeoutParam = req.getParameter("timeout");
		final String xParam = req.getParameter("x");
		final String yParam = req.getParameter("y");

		if (operation == null || (! OPERATIONS.contains(operation.toUpperCase()))) {
			displayUsage(resp);
			return;
		}

		final int x;
		try {
			x = Integer.parseInt(xParam);
		} catch (final Exception e) {
			displayUsage(resp);
			return;
		}

		final int y;
		try {
			y = Integer.parseInt(yParam);
		} catch (final Exception e) {
			displayUsage(resp);
			return;
		}

		int delay = 0;
		try {
			delay = Integer.parseInt(delayParam);
		} catch (final Exception e) {
			// ignore
		}

		int timeout = -1;
		try {
			timeout = Integer.parseInt(timeoutParam);
		} catch (final Exception e) {
			// ignore
		}

		boolean async = false;
		try {
			async = Boolean.parseBoolean(asyncParam);
		} catch (final Exception e) {
			// ignore
		}

		final PrintWriter writer = resp.getWriter();

		if (! async) {
			process(operation, x, y, writer);
			return;
		}

		final int threadDelay = delay;
		final AsyncContext asyncContext = req.startAsync();
		asyncContext.setTimeout(timeout);
		executorService.submit(new Runnable() {
			@Override
			public void run() {

				try {
					Thread.sleep(threadDelay);
				} catch (final InterruptedException e) {
					// ignore
				}

				try {
					process(operation, x, y, writer);
				} catch (final Exception e) {

				} finally {
					asyncContext.complete();
				}

			}
		});
	}

	private void process(final String operation, final int x, final int y, final PrintWriter writer) throws IOException {
		if ("ADD".equals(operation.toUpperCase())) {
			writer.print(bean.add(x, y));
		} else if ("SUBTRACT".equals(operation.toUpperCase())) {
			writer.print(bean.subtract(x, y));
		} else if ("MULTIPLY".equals(operation.toUpperCase())) {
			writer.print(bean.multiply(x, y));
		} else if ("DIVIDE".equals(operation.toUpperCase())) {
			writer.print(bean.divide(x, y));
		}
	}


	void displayUsage(final HttpServletResponse resp) throws IOException {
		resp.getWriter().println("Usage information will appear here");
	}

	
}
