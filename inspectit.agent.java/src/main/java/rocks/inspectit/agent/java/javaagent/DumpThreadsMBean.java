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

// import com.sun.media.jfxmedia.logging.Logger;

/**
 * @author AsrarGadi
 *
 */

public class DumpThreadsMBean {


	// private static final Logger LOGGER = Logger.getLogger(JavaAgent.class.getName());
	/**
	 * @param args
	 */
	public static void dumpThreads(int traceint, String threadname) {

		// final StringBuilder threadString = new StringBuilder();
		final ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
		final ThreadInfo[] infos = threadMXBean.getThreadInfo(threadMXBean.getAllThreadIds(), Integer.MAX_VALUE);

		// to be able to exclude daemon thread, getting all threads and ids
		//Map threads = Thread.getAllStackTraces();
		Map<Thread, StackTraceElement[]> threads = Thread.getAllStackTraces();
		Map<Long, Thread> threadIds = new HashMap<Long, Thread>();

		// for (Iterator<Thread> itr = threads.keySet().iterator(); itr.hasNext();) {
		// Thread th = (Thread) itr.next();
		for (Thread thread : threads.keySet()) {
			if (thread.getName().equals(threadname)) {
				threadIds.put(thread.getId(), thread);
			}
		}

		// list for adding methods
		List<String> uniqueMethodsList = new ArrayList<String>();
		List<String> allMethodsList = new ArrayList<String>();

		for (ThreadInfo info : infos) {
			if (info.getThreadName() == threadname) {

				// excluding all daemon threads
				// if (threadIds.get(info.getThreadId()).isDaemon() == false) {
				// thread name
				// threadString.append("Thread \"");

				long tID = info.getThreadId();
				// threadString.append(tID);

				// threadString.append("\" ");

				String tName = info.getThreadName();
				// threadString.append(tName);
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
					allMethodsList.add(mName);
					if (!uniqueMethodsList.contains(mName)) {
						uniqueMethodsList.add(mName);
					}


					/*
					 * Class mClass = stackTraceElement.getMethodName().getClass(); int mLineNumber
					 * = stackTraceElement.getLineNumber(); MethodDetails currMeth = new
					 * MethodDetails(mName, mClass.getName(), mLineNumber); MethodDetails
					 * methodsList[] = new MethodDetails[stackTraceElements.length]; int mcount = 0;
					 * for (MethodDetails element : methodsList) { if(element.equals(currMeth)) {
					 * mcount = mcount+1; } }
					 */

					// threadString.append(mName);

					// threadString.append(", at ");
					// threadString.append(stackTraceElement);
				}
				// threadString.append("\n\n");
				// }

				// printThreads(threadString.toString());
				// LOGGER.info(threadString.toString());
			}
		}

		showTopMethods(allMethodsList);

		try {
			Thread.sleep(traceint);
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
			/* ignore */}
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
		args = new String[] { "200", "500", "Gauss", "Matrixx", "10", "200" };
		try {
			// to measure the execution time of the code

			int tthreads = getIntArg(args[0], 1, 1000, "Invalid monitored threads, must be between 1 and total number of threads");
			int traceint = getIntArg(args[1], 1, 1000, "Invalid trace interval, must be between 1 and 1000 ms");
			String threadname = args[2];
			Class mainclass = Class.forName("mytest.Matrixx");
			Method mainmeth = mainclass.getMethod("main", args.getClass());
			String copied[] = Arrays.copyOfRange(args, 4, args.length);
			mainmeth.invoke(null, (Object) copied);

			long startTime = System.currentTimeMillis();
			dumpThreads(traceint, threadname);

			// to measure the execution time of the code and print it
			long stopTime = System.currentTimeMillis();
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
	}

	// ###################################################################################################################
	public static void showTopMethods(List<String> methodIDs) {
		long startTime = System.currentTimeMillis();
		System.out.printf("*************************************************************************************** %n");
		Map<String, Integer> topIDs = new HashMap<>();
		methodIDs.stream().distinct().forEach((e) -> {
			topIDs.put(e, 0);
		});
		methodIDs.stream().forEach((e) -> {
			topIDs.put(e, topIDs.get(e) + 1);
		});
		topIDs.entrySet().stream().sorted((e1, e2) -> e2.getValue() - e1.getValue()).limit(5).forEach((e) -> {
			System.out.printf("Name: %s ... Frequency: %d ... Percentage: %.2f%% ...   %n", e.getKey(), topIDs.get(e.getKey()), ((1.0 * e.getValue()) / methodIDs.size()) * 100);
		});
		System.out.printf("*************************************************************************************** %n");
		long stopTime = System.currentTimeMillis();
		System.out.println("############################### ShowTopMethod(): Elapsed time was " + (stopTime - startTime) + " miliseconds. ###############################");

	}

	/*
	 * public class MethodDetails { private String methodName; private String methodClass; private
	 * Integer methodLineNumber; public MethodDetails(String mName, String mClass, int mLine) {
	 * this.methodName = mName; this.methodClass = mClass; this.methodLineNumber = mLine; } public
	 * void addMethod(String mName, String mClass, int mLine) { this.methodName = mName;
	 * this.methodClass = mClass; this.methodLineNumber = mLine; } public String getmethodName() {
	 * return methodName; } public void setmethodName(String methodName) { this.methodName =
	 * methodName; } public String getmethodClass() { return methodClass; } public void
	 * setmethodClass(String methodClass) { this.methodClass = methodClass; } public Integer
	 * getmethodLineNumber() { return methodLineNumber; } public void setmethodLineNumber(Integer
	 * methodLineNumber) { this.methodLineNumber = methodLineNumber; } }
	 */

}
