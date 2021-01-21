package com.utils;

import java.util.Properties;

import org.apache.log4j.PropertyConfigurator;

/**
 * <pre>
 * 日志工具类
 * 1、配置在代码中初始化，运维无需配置文件
 * 2、获取堆栈性能优化
 * </pre>
 * 
 * @author reison
 * @time 2017年2月18日 下午4:54:04
 */
public class Log {

	private static final String NEW_LINE = "\r\n";
	private static org.apache.log4j.Logger logger = null;
	private static final String SELF_NAME = Log.class.getName();
	private static final Properties pro = new Properties();

	public final static boolean init(Class<?> clazz) {
		final String serverName = clazz.getSimpleName().toLowerCase();
		boolean isProduction = "production".equals(System.getenv("CUR_ENV"));
		logger = org.apache.log4j.Logger.getLogger("");
		// pro.put("log4j.rootLogger", "CONSOLE,DEBUG,INFO,ERROR,FATAL");
		pro.put("log4j.rootLogger", "DEBUG,IC,I,E,F");
		pro.put("log4j.addivity.org.apache", "true");
		pro.put("log4j.appender.A2.BufferedIO", "true");
		pro.put("log4j.appender.A2.BufferSize", "10240");

		pro.put("log4j.appender.IC", "org.apache.log4j.ConsoleAppender");
		pro.put("log4j.appender.IC.Threshold", "INFO");
		pro.put("log4j.appender.IC.Target", "System.out");
		pro.put("log4j.appender.IC.layout", "org.apache.log4j.PatternLayout");
		pro.put("log4j.appender.IC.layout.ConversionPattern", "[%-5p]:%d-%c-%-2r[%t]%x%n%m %n");
		//
		// pro.put("log4j.appender.DEBUG", "org.apache.log4j.DailyRollingFileAppender");
		// pro.put("log4j.appender.DEBUG.Threshold", "DEBUG");
		// if (isProduction) {
		// pro.put("log4j.appender.DEBUG.File", serverName + "/log/debug.log");
		// } else {
		// pro.put("log4j.appender.DEBUG.File", "log/debug.log");
		// }
		// pro.put("log4j.appender.DEBUG.DatePattern", "'.'yyyyMMdd");
		// pro.put("log4j.appender.DEBUG.Append", "true");
		// pro.put("log4j.appender.DEBUG.layout", "org.apache.log4j.PatternLayout");
		// pro.put("log4j.appender.DEBUG.layout.ConversionPattern", "%-5p:%d-%c-%-2r[%t]%x%n%m %n");

		pro.put("log4j.appender.I", "org.apache.log4j.DailyRollingFileAppender");
		pro.put("log4j.appender.I.Threshold", "INFO");
		if (isProduction) {
			pro.put("log4j.appender.I.File", serverName + "/log/info.log");
		} else {
			pro.put("log4j.appender.I.File", "log/info.log");
		}
		pro.put("log4j.appender.I.DatePattern", "'.'yyyyMMdd");
		pro.put("log4j.appender.I.Append", "true");
		pro.put("log4j.appender.I.layout", "org.apache.log4j.PatternLayout");
		pro.put("log4j.appender.I.layout.ConversionPattern", "[%-5p]:%d-%c-%-2r[%t]%x%n%m %n");

		pro.put("log4j.appender.E", "org.apache.log4j.DailyRollingFileAppender");
		pro.put("log4j.appender.E.Threshold", "ERROR");
		if (isProduction) {
			pro.put("log4j.appender.E.File", serverName + "/log/error.log");
		} else {
			pro.put("log4j.appender.E.File", "log/error.log");
		}
		pro.put("log4j.appender.E.DatePattern", "'.'yyyyMMdd");
		pro.put("log4j.appender.E.Append", "true");
		pro.put("log4j.appender.E.layout", "org.apache.log4j.PatternLayout");
		pro.put("log4j.appender.E.layout.ConversionPattern", "[%-5p]:%d-%c-%-2r[%t]%x%n%m %n");

		pro.put("log4j.appender.F", "org.apache.log4j.DailyRollingFileAppender");
		pro.put("log4j.appender.F.Threshold", "FATAL");
		if (isProduction) {
			pro.put("log4j.appender.F.File", serverName + "/log/fatal.log");
		} else {
			pro.put("log4j.appender.F.File", "log/fatal.log");
		}
		pro.put("log4j.appender.F.DatePattern", "'.'yyyyMMdd");
		pro.put("log4j.appender.F.Append", "true");
		pro.put("log4j.appender.F.layout", "org.apache.log4j.PatternLayout");
		pro.put("log4j.appender.F.layout.ConversionPattern", "[%-5p]:%d-%c-%-2r[%t]%x%n%m %n");

		PropertyConfigurator.configure(pro);
		return true;
	}

	/**
	 * <pre>
	 * 获取堆栈信息
	 * </pre>
	 *
	 * @param msg
	 * @return
	 */
	private final static String getStackMsg(String msg) {
		StackTraceElement last = null;
		final String thisClazz = SELF_NAME;
		final StackTraceElement[] stackTrace = new Throwable().getStackTrace();
		for (int i = stackTrace.length - 1; i > 0; i--) {
			final String className = stackTrace[i].getClassName();
			if (thisClazz.equals(className)) {
				last = stackTrace[i + 1];
				return last == null ? "no stack" : last.toString() + "-" + msg.toString();
			}
		}
		return "";
	}

	@SuppressWarnings("unused")
	private static Object getStackMsgOld(Object msg) {
		StackTraceElement[] ste = Thread.currentThread().getStackTrace();
		if (ste == null) {
			return "";
		}

		boolean srcFlag = false;
		for (int i = 0; i < ste.length; i++) {
			StackTraceElement s = ste[i];
			// 如果上一行堆栈代码是本类的堆栈，则该行代码则为源代码的最原始堆栈。
			if (srcFlag) {
				return s == null ? "" : s.toString() + NEW_LINE + msg.toString();
			}
			// 定位本类的堆栈
			if (SELF_NAME.equals(s.getClassName())) {
				srcFlag = true;
				i++;
			}
		}
		return "";
	}

	public static void debug(String msg) {
		String message = getStackMsg(msg);
		logger.debug(message);
	}

	public static void debug(String msg, Throwable t) {
		String message = getStackMsg(msg);
		logger.debug(message, t);
	}

	public static void info(String msg) {
		String message = getStackMsg(msg);
		logger.info(message);
	}

	public static void info(String msg, Throwable t) {
		String message = getStackMsg(msg);
		logger.info(message, t);
	}

	public static void warn(String msg) {
		String message = getStackMsg(msg);
		logger.warn(message);
	}

	public static void warn(String msg, Throwable t) {
		String message = getStackMsg(msg);
		logger.warn(message, t);
	}

	public static void error(String msg) {
		String message = getStackMsg(msg);
		logger.error(message);
	}

	public static void error(String msg, Throwable t) {
		String message = getStackMsg(msg);
		if (logger != null) {
			logger.error(message, t);
		}
	}

	public static void fatal(String msg) {
		String message = getStackMsg(msg);
		logger.fatal(message);
	}

	public static void fatal(String msg, Throwable t) {
		String message = getStackMsg(msg);
		logger.fatal(message, t);
	}

	/**** =============================== Cmd或Action执行时间日志部分start ========================== ***/
	/**
	 * <pre>
	 * 记录消耗时间
	 * !name中不能出现：--
	 * </pre>
	 * 
	 * @param name
	 * @param curTime
	 * @param totalTime
	 * @param queueSize
	 */
	public static void logTime(String name, long curTime, long totalTime, int queueSize, String comment) {
		Log.fatal("-LogTime--Date=" + TimeUtil.getDateFormat() + "&name=" + name + "&curTime=" + curTime + "&totalTime=" + totalTime + "&queueSize=" + queueSize + "&comment=" + comment);
	}

	public static void logTime(Class<?> clazz, long curTime, long totalTime, int queueSize, String comment) {
		logTime(clazz.getSimpleName(), curTime, totalTime, queueSize, comment);
	}

	public static void logTime(Object obj, long curTime, long totalTime, int queueSize, String comment) {
		logTime(obj.getClass().getSimpleName(), curTime, totalTime, queueSize, comment);
	}
	/**** =============================== Cmd或Action执行时间日志部分end ========================== ***/

	/**** =============================== 对象创建和销毁的日志部分start ========================== ***/

	/**
	 * <pre>
	 * 记录创建日志(用于监测对象是否销毁)
	 * </pre>
	 * 
	 * @param name
	 * @param comment
	 */
	public static String logCreateObj(String name, String comment) {
		String idx = RandomStrUtil.randomStr(8);
		Log.debug("-LogCreateObj--Index=" + idx + "&Date=" + TimeUtil.getDateFormat() + "&Name=" + name + "&Comment=" + comment);
		return idx;
	}

	/**
	 * <pre>
	 * 记录销毁日志(用于监测对象是否销毁)
	 * </pre>
	 * 
	 * @param idx 创建时的唯一索引
	 * @param name
	 * @param comment
	 */
	public static void logGCObj(String idx, String name, String comment) {
		Log.debug("-LogGCObj--Index=" + idx + "&Date=" + TimeUtil.getDateFormat() + "&Name=" + name + "&Comment=" + comment);
	}

	/**
	 * <pre>
	 * 命令行日志
	 * </pre>
	 *
	 * @param msg
	 */
	public final static void logConsole(String msg) {
		System.out.println(TimeUtil.getDateFormat() + "&&&" + msg);
	}
}
