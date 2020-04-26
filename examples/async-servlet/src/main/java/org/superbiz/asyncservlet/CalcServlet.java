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

import jakarta.ejb.EJB;
import jakarta.servlet.AsyncContext;
import jakarta.servlet.AsyncEvent;
import jakarta.servlet.AsyncListener;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@WebServlet(urlPatterns = "/*", asyncSupported = true)
public class CalcServlet extends HttpServlet {

	public static final String RESULT_ATTRIBUTE = "RESULT";

	@EJB
	private CalcBean bean;

	private static final List<String> OPERATIONS = Arrays.asList("ADD", "SUBTRACT", "MULTIPLY", "DIVIDE");

	@Override
	protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
		process(req, resp);
	}


	@Override
	protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
		process(req, resp);
	}

	private void process(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {

		if (req.getAttribute(RESULT_ATTRIBUTE) == null) {

			final ResultHolder result = new ResultHolder();
			req.setAttribute("RESULT", result);

			final String operation = req.getParameter("op");
			final String asyncParam = req.getParameter("async");
			final String delayParam = req.getParameter("delay");
			final String timeoutParam = req.getParameter("timeout");
			final String xParam = req.getParameter("x");
			final String yParam = req.getParameter("y");

			if (operation == null || (!OPERATIONS.contains(operation.toUpperCase()))) {
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

			if (!async) {
				process(operation, x, y, result);
				resp.getWriter().print(result.getResult());
				return;
			}

			final int threadDelay = delay;
			final AsyncContext asyncContext = req.startAsync();

			final AtomicBoolean interrupted = new AtomicBoolean(false);

			asyncContext.addListener(new AsyncListener() {
				@Override
				public void onComplete(AsyncEvent asyncEvent) throws IOException {

				}

				@Override
				public void onTimeout(AsyncEvent asyncEvent) throws IOException {
					interrupted.set(true);
				}

				@Override
				public void onError(AsyncEvent asyncEvent) throws IOException {
					interrupted.set(true);
				}

				@Override
				public void onStartAsync(AsyncEvent asyncEvent) throws IOException {

				}
			});
			asyncContext.setTimeout(timeout);
			asyncContext.start(() -> {

				try {
					Thread.sleep(threadDelay);
				} catch (final InterruptedException e) {
					// ignore
				}

				try {
					process(operation, x, y, result);
				} finally {
					if (! interrupted.get()) {

						// do not call dispatch if this request has timed-out or errored
						asyncContext.dispatch();
					}
				}

			});
		} else {
			final ResultHolder result = (ResultHolder) req.getAttribute("RESULT");
			resp.getWriter().print(result.getResult());
		}
	}

	private void process(final String operation, final int x, final int y, final ResultHolder result) {
		if ("ADD".equals(operation.toUpperCase())) {
			result.setResult(bean.add(x, y));
		} else if ("SUBTRACT".equals(operation.toUpperCase())) {
			result.setResult(bean.subtract(x, y));
		} else if ("MULTIPLY".equals(operation.toUpperCase())) {
			result.setResult(bean.multiply(x, y));
		} else if ("DIVIDE".equals(operation.toUpperCase())) {
			result.setResult(bean.divide(x, y));
		}
	}

	void displayUsage(final HttpServletResponse resp) throws IOException {
		resp.getWriter().println("Parameters:");
		resp.getWriter().println("\tx: 1st Operand");
		resp.getWriter().println("\ty: 2nd Operand");
		resp.getWriter().println("\top: Operator - add | subtract | multiply | divide");
		resp.getWriter().println("\tasync: Whether to run asynchronously - true | false");
		resp.getWriter().println("\tdelay: Delay for async calls (in ms)");
		resp.getWriter().println("\ttimeout: Timeout for async calls (in ms)\n\n");
		resp.getWriter().println("Example:");
		resp.getWriter().println("\tSync: http://localhost:8080/async-servlet?x=2&y=4&op=multiply");
		resp.getWriter().println("\tAsync (1 second delay): http://localhost:8080/async-servlet?x=2&y=4&op=multiply&async=true&delay=1000");
		resp.getWriter().println("\tAsync Timeout (10 second delay, 1 second timeout): http://localhost:8080/async-servlet?x=2&y=4&op=multiply&async=true&delay=10000&timeout=1000");
	}

	public static class ResultHolder {
		private int result = 0;

		public int getResult() {
			return result;
		}

		public void setResult(final int result) {
			this.result = result;
		}
	}
}
