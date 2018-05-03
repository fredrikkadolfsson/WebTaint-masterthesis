package se.adolfsson.dtp.utils.api;


import java.lang.reflect.Field;

public class TaintTools {
	public static void taint(Object s) {
		setTaint(s, true);
	}

	public static void detaint(Object s) {
		setTaint(s, false);
	}

	private static void setTaint(Object s, boolean value) {
		if (s instanceof Taintable) {
			((Taintable) s).setTaint(value);
		}
	}

	private static Field taintField(Object s) throws NoSuchFieldException {
		return s.getClass().getField("tainted");
	}

	public static boolean isTainted(Object s) {
		return s instanceof Taintable && ((Taintable) s).isTainted();
	}

	public static void checkTaint(Object s, String signature) {
		//if (isTainted(s)) throw new TaintException(s.toString(), signature);
	}
}

