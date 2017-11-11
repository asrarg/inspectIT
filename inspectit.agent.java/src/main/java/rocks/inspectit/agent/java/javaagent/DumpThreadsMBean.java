package rocks.inspectit.agent.java.javaagent;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;

// import com.sun.media.jfxmedia.logging.Logger;

/**
 * @author AsrarGadi
 *
 */
public class DumpThreadsMBean {

	private static final Logger LOGGER = Logger.getLogger(JavaAgent.class.getName());
	/**
	 * @param args
	 */
	public static void DumpThreads() {
		final StringBuilder threadString = new StringBuilder();
		final ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
		final ThreadInfo[] infos = threadMXBean.getThreadInfo(threadMXBean.getAllThreadIds(), Integer.MAX_VALUE);

		// to be able to exclude daemon thread, getting all threads and ids
		Map threads = Thread.getAllStackTraces();
		Map<Long, Thread> threadIds = new HashMap();
		for (Iterator itr = threads.keySet().iterator(); itr.hasNext();) {
			Thread th = (Thread) itr.next();
			threadIds.put(th.getId(), th);
		}

		for (ThreadInfo info : infos) {
			// excluding all daemon threads
			if (threadIds.get(info.getThreadId()).isDaemon() == false) {
				// thread name
				threadString.append("Thread \"");
				threadString.append(info.getThreadName());
				threadString.append("\" ");
				// thread state
				final Thread.State _state = info.getThreadState();
				threadString.append(_state);
				// stack trace for it
				final StackTraceElement[] stackTraceElements = info.getStackTrace();
				for (final StackTraceElement stackTraceElement : stackTraceElements) {
					threadString.append("\n    at ");
					threadString.append(stackTraceElement);
				}
				threadString.append("\n\n");
			}
			LOGGER.info(threadString.toString());

		}
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
