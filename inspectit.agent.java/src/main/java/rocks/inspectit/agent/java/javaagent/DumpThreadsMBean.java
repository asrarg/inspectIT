package rocks.inspectit.agent.java.javaagent;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Objects;

// import com.sun.media.jfxmedia.logging.Logger;

/**
 * @author AsrarGadi
 *
 */

public class DumpThreadsMBean {


	// private static final Logger LOGGER = Logger.getLogger(JavaAgent.class.getName());
	static boolean terminated = false;
	static List<String> allMethodsList = new ArrayList<String>();
	/**
	 * @param args
	 */
	public static void dumpThreads(int traceint) {
		final StringBuilder threadString = new StringBuilder();
		final ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
		final ThreadInfo[] infos = threadMXBean.getThreadInfo(threadMXBean.getAllThreadIds(), Integer.MAX_VALUE);
		Map<Thread, StackTraceElement[]> threads = Thread.getAllStackTraces();
		// list for adding methods
		List<String> uniqueMethodsList = new ArrayList<String>();

		// while (!terminated) {
		try {
			List<ThreadInfo> aliveThreads = new ArrayList<ThreadInfo>();
			Thread.sleep(traceint);
			for (ThreadInfo info : infos) {
				// for (int inf = 0; inf < 1; inf++) {
				if ((info == null) || !Objects.equal(info.getThreadName(), "Gauss")) {
					continue;
				}
				long tID = info.getThreadId();

				// collecting monitored threads in a list to check whether they're
				// terminated later on or not at end of loop.
				aliveThreads.add(info);

				// thread name
				threadString.append("Thread \"");
				threadString.append(tID);
				threadString.append("\" ");

				String tName = info.getThreadName();
				threadString.append(tName);
				// thread state
				final Thread.State threadState = info.getThreadState();
				// threadString.append(" " + threadState);

				// stack trace for it
				final StackTraceElement[] stackTraceElements = info.getStackTrace();
				for (final StackTraceElement stackTraceElement : stackTraceElements) {
					// threadString.append("\n");
					// threadString.append("StackTrace of Thread: " + tID);
					// threadString.append(" \n Method: ");

					String mName = stackTraceElement.getMethodName();
					synchronized (allMethodsList) {
						allMethodsList.add(mName);
					}
					if (!uniqueMethodsList.contains(mName)) {
						uniqueMethodsList.add(mName);
					}
					// threadString.append(mName);
					// threadString.append(", at ");
					// threadString.append(stackTraceElement);
				}
				// threadString.append("\n\n");

				// printThreads(threadString.toString());
				// LOGGER.info(threadString.toString());

			}
			terminated = aliveThreads.isEmpty();
		} catch (InterruptedException ex) {
			ex.printStackTrace();
		}
		// }


	}

	// ###################################################################################################################
	public static void printThreads(String xx) {
		Writer writer = null;
		try {
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("C:\\Users\\agd\\inspectit-master2\\ws\\DummyProject\\StackTrace.txt"), "utf-8"));
			String toPrint = xx;
			writer.write(toPrint);
			writer.write("\n");

		} catch (IOException ex) {
			System.out.println("Error while writting StackTrace in file..");
		} finally {
			try {
				writer.close();
			} catch (Exception ex) {
				/* ignore */
			}
		}
	}

	// ###################################################################################################################
	private static int getIntArg(String arg, int min, int max, String mesg) {
		try {
			int result = Integer.parseInt(arg);
			if ((result < min) || (result > max)) {
				System.err.println(mesg);
				System.exit(1);
			}
			return result;
		} catch (NumberFormatException ex) {
			System.err.println(String.format("Invalid integer input %s", arg));
			System.exit(1);
		}
		return -1;
	}

	// ################################################### MAIN
	// ################################################################
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println("Agent Launched..");
		args = new String[] { "200", "100", "Gauss", "Matrixx", "10", "200" };
		try {
			// to measure the execution time of the code

			// int tthreads = getIntArg(args[0], 1, 1000, "Invalid monitored threads, must be
			// between 1 and total number of threads");
			int traceint = getIntArg(args[1], 1, 1000, "Invalid trace interval, must be between 1 and 1000 ms");
			// String threadname = args[2];
			Class mainclass = Class.forName("mytest.Matrixx");
			Method mainmeth = mainclass.getMethod("main", args.getClass());
			String copied[] = Arrays.copyOfRange(args, 4, args.length);
			mainmeth.invoke(null, (Object) copied);

			long startTime = System.currentTimeMillis();

			Thread t = new Thread(new Runnable() {
				@Override
				public void run() {
					showTopMethods();
				}
			});

			t.start();

			while (!terminated) {
				dumpThreads(traceint);
			}

			long stopTime = System.currentTimeMillis();

			t.join();
			// to measure the execution time of the code and print it
			System.out.println("############################### Java Agent MAIN(): Elapsed time was " + (stopTime - startTime) + " miliseconds. ###############################");

		} catch (ClassNotFoundException ex) {
			ex.printStackTrace(System.err);
		} catch (NoSuchMethodException ex) {
			ex.printStackTrace(System.err);
		} catch (IllegalAccessException ex) {
			ex.printStackTrace(System.err);
		} catch (Exception ex) {
			ex.printStackTrace(System.err);
		}

		System.out.println("Done.");
	}

	// ###################################################################################################################
	public static void showTopMethods() {

		while (!terminated) {
			long startTime = System.currentTimeMillis();
			try {
				Thread.sleep(1000);
				System.out.printf("*************************************************************************************** %n");
				List<String> oldList = new ArrayList<String>();
				synchronized (allMethodsList) {
					oldList.addAll(allMethodsList);
					allMethodsList.clear();
				}

				Map<String, Integer> topIDs = new HashMap<>();
				oldList.stream().distinct().forEach((e) -> {
					topIDs.put(e, 0);
				});
				oldList.stream().forEach((e) -> {
					topIDs.put(e, topIDs.get(e) + 1);
				});
				topIDs.entrySet().stream().sorted((e1, e2) -> e2.getValue() - e1.getValue()).limit(5).forEach((e) -> {
					System.out.printf("Name: %s ... Frequency: %d ... Percentage: %.2f%% ...   %n", e.getKey(), topIDs.get(e.getKey()), ((1.0 * e.getValue()) / oldList.size()) * 100);
				});
				System.out.printf("*************************************************************************************** %n");
				long stopTime = System.currentTimeMillis();
				System.out.println("############################### ShowTopMethod(): Elapsed time was " + (stopTime - startTime) + " miliseconds. ###############################");

			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("*** ERROR WHILE PRINTING CALCULATIONS ***");
			}
		}
	}

}
