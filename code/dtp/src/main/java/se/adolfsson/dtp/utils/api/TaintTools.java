package se.adolfsson.dtp.utils.api;


import java.lang.reflect.Field;

public class TaintTools {
	public static void taint(Object s, String className) {
		setTaint(s, true, className);
	}

	public static void detaint(Object s) {
		setTaint(s, false, null);
	}

	private static void setTaint(Object s, boolean value, String className) {
		if (s instanceof Taintable) {
			((Taintable) s).setTaint(value, className);
		}
	}

	private static Field taintField(Object s) throws NoSuchFieldException {
		return s.getClass().getField("tainted");
	}

	public static boolean isTainted(Object s) {
		return s instanceof Taintable && ((Taintable) s).isTainted();
	}

	public static void checkTaint(Object s, String className) {
		if (isTainted(s)) {
			((Taintable) s).setTaint(false, null);
			System.out.println("Taint Exception Caught!!!!\r\n\tSource: " + ((Taintable) s).getTaintSource() + "\r\n\tSink: " + className);
			//System.out.println("Tainted object entering Sink!: " + s.toString().substring(0, 10) + "(END)");
			//Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).warning("Tainted object entering Sink!");
			//throw new TaintException(s.toString(), signature);
		}
	}
}

