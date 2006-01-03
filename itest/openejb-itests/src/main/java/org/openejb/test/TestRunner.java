package org.openejb.test;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.util.Properties;

import junit.framework.TestResult;

import org.openejb.util.JarUtils;

/**
 * 
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @author <a href="mailto:Richard@Monson-Haefel.com">Richard Monson-Haefel</a>
 */
public class TestRunner extends junit.textui.TestRunner {
	private static final String helpBase = "META-INF/org.openejb.cli/";

	/**
	 * Constructs a TestRunner.
	 */
	public TestRunner() {
		this(System.out);
	}

	/**
	 * Constructs a TestRunner using the given stream for all the output
	 */
	public TestRunner(PrintStream writer) {
		this(new ResultPrinter(writer));
	}

	/**
	 * Constructs a TestRunner using the given ResultPrinter all the output
	 */
	public TestRunner(ResultPrinter printer) {
		super(printer);
	}

	/**
	 * main entry point.
	 */
	public static void main(String args[]) {
		if (args.length == 0) {
			printHelp();
		} else {
			if (args[0].equals("--help")) {
				printHelp();

				return;
			} else if (args[0].equals("local")) {
				runLocalTests();
			} else if (args[0].equals("remote")) {
				runRemoteTests();
			} else {
				printHelp();

				return;
			}

			try {
				TestRunner aTestRunner = new TestRunner();
				TestResult r = aTestRunner
						.start(new String[] { "org.openejb.test.ClientTestSuite" });
				if (!r.wasSuccessful())
					System.exit(FAILURE_EXIT);
				System.exit(SUCCESS_EXIT);
			} catch (Exception e) {
				System.err.println(e.getMessage());
				System.exit(EXCEPTION_EXIT);
			}
		}
	}

	private static void runLocalTests() {
		System.setProperty("openejb.test.server",
				"org.openejb.test.IvmTestServer");
		System.setProperty("openejb.test.database",
				"org.openejb.test.InstantDbTestDatabase");

		System.out.println("_________________________________________________");
		System.out
				.println("|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|\n");
		System.out.println("Running EJB compliance tests on IntraVM Server");
		System.out.println("_________________________________________________");
	}

	private static void runRemoteTests() {
		System.setProperty("openejb.test.server",
				"org.openejb.test.RemoteTestServer");
		System.setProperty("openejb.test.database",
				"org.openejb.test.InstantDbTestDatabase");

		System.out.println("_________________________________________________");
		System.out
				.println("|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|_|\n");
		System.out.println("Running EJB compliance tests on Remote Server");
		System.out.println("_________________________________________________");
	}

	private static void printHelp() {
		String header = "OpenEJB Compliance Tests ";
		try {
			JarUtils.setHandlerSystemProperty();
			Properties versionInfo = new Properties();
			versionInfo.load(new URL("resource:/openejb-version.properties")
					.openConnection().getInputStream());
			header += versionInfo.get("version");
		} catch (java.io.IOException e) {
		}

		System.out.println(header);

		// Internationalize this
		try {
			InputStream in = Thread.currentThread().getContextClassLoader()
					.getResource(helpBase + "test.help").openConnection()
					.getInputStream();

			int b = in.read();
			while (b != -1) {
				System.out.write(b);
				b = in.read();
			}
		} catch (java.io.IOException e) {
		}
	}

	public TestResult start(String args[]) throws Exception {
		TestResult result = null;
		try {

			TestManager.init(null);
			TestManager.start();
		} catch (Exception e) {
			System.out.println("Cannot initialize the test environment: "
					+ e.getClass().getName() + " " + e.getMessage());
			// e.printStackTrace();
			// System.exit(-1);
			throw e;
		}

		try {
			result = super.start(args);
		} catch (Exception ex) {
		} finally {
			try {
				TestManager.stop();
			} catch (Exception e) {
				; // ignore it
			}
		}
		// System.exit(0);
		return result;
	}

	private static final class Pipe implements Runnable {

		private final InputStream is;

		private final OutputStream out;

		private Pipe(InputStream is, OutputStream out) {

			super();

			this.is = is;

			this.out = out;

		}

		public void run() {

			try {

				int i = is.read();

				out.write(i);

				while (i != -1) {

					i = is.read();

					out.write(i);

				}

			} catch (Exception e) {

				e.printStackTrace();

			}

		}

	}
}
