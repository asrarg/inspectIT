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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;

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
		// list for adding methods
		List<String> uniqueMethodsList = new ArrayList<String>();
		try {
			List<ThreadInfo> aliveThreads = new ArrayList<ThreadInfo>();
			Thread.sleep(traceint);
			for (ThreadInfo info : infos) {
				if (((info == null)) || !info.getThreadName().equals("Gauss")) {
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
				threadString.append(" " + threadState);
				// stack trace for it
				final StackTraceElement[] stackTraceElements = info.getStackTrace();
				for (final StackTraceElement stackTraceElement : stackTraceElements) {
					threadString.append("\n");
					threadString.append("StackTrace of Thread: " + tID);
					threadString.append(" \n Method: ");
					String mName = stackTraceElement.getMethodName();
					synchronized (allMethodsList) {
						allMethodsList.add(mName);
					}
					if (!uniqueMethodsList.contains(mName)) {
						uniqueMethodsList.add(mName);
					}
					threadString.append(mName);
					threadString.append(", at ");
					threadString.append(stackTraceElement);
				}
				threadString.append("\n\n");
				printThreads(threadString.toString());
				// LOGGER.info(threadString.toString());
			}
			terminated = aliveThreads.isEmpty();
		} catch (InterruptedException ex) {
			ex.printStackTrace();
		}
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
				System.out.println("Error while closing StackTrace file..");
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

	// ###################################################
	// MAIN################################################################
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println("Agent Launched..");
		args = new String[] { "10" };
		try {
			int traceint = getIntArg(args[0], 1, 1000, "Invalid trace interval, must be between 1 and 1000 ms");
			Class<?> mainclass = Class.forName("rocks.inspectit.agent.java.javaagent.Matrixx");
			Method mainmeth = mainclass.getMethod("main", args.getClass());
			String[] copied = Arrays.copyOfRange(args, 1, args.length);
			mainmeth.invoke(null, (Object) copied);
			// to measure the execution time of the code
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
			// to measure the execution time of the code and print it
			long stopTime = System.currentTimeMillis();
			t.join();
			System.out.println("############################### Java Agent MAIN(): Elapsed time was " + (stopTime - startTime) + " miliseconds. ###############################");

		} catch (Exception ex) {
			ex.printStackTrace(System.err);
		}
		System.out.println("Done.");
	}
	// ###################################################################################################################
	private static <K, V extends Comparable<? super V>> List<Entry<K, V>> findGreatest(Map<K, V> map, int n) {
		Comparator<? super Entry<K, V>> comparator = new Comparator<Entry<K, V>>() {
			@Override
			public int compare(Entry<K, V> e0, Entry<K, V> e1) {
				V v0 = e0.getValue();
				V v1 = e1.getValue();
				return v0.compareTo(v1);
			}
		};
		PriorityQueue<Entry<K, V>> highest = new PriorityQueue<Entry<K, V>>(n, comparator);
		for (Entry<K, V> entry : map.entrySet()) {
			highest.offer(entry);
			while (highest.size() > n) {
				highest.poll();
			}
		}

		List<Entry<K, V>> result = new ArrayList<Map.Entry<K, V>>();
		while (highest.size() > 0) {
			result.add(highest.poll());
		}
		return result;
	}
	// ###################################################################################################################
	public static void showTopMethods() {
		while (!terminated) {
			try {
				Thread.sleep(1000);
				System.out.printf("*************************************************************************************** %n");
				List<String> oldList = new ArrayList<String>();
				synchronized (allMethodsList) {
					oldList.addAll(allMethodsList);
					allMethodsList.clear();
				}
				Map<String, Integer> freq = new HashMap<String, Integer>();
				for (String s : oldList) {
					int count = 0;
					// get previous count
					if (freq.get(s) != null) {
						count = freq.get(s);
					}
					freq.put(s, count + 1);
				}
				int n = 5;
				List<Entry<String, Integer>> greatest = findGreatest(freq, 5);
				for (Entry<String, Integer> e : greatest) {
					System.out.printf("Name: %s ... Frequency: %d ... Percentage: %.2f%% ...   %n", e.getKey(), freq.get(e.getKey()), ((1.0 * e.getValue()) / oldList.size()) * 100);
				}
				System.out.printf("*************************************************************************************** %n");
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("*** ERROR WHILE PRINTING CALCULATIONS ***");
			}
		}
	}

}
