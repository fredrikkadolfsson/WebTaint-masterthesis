package se.adolfsson.dtp.utils;

import se.adolfsson.dtp.utils.api.Taintable;

import static se.adolfsson.dtp.utils.api.TaintTools.*;

public class TaintUtils {
	public static boolean propagateParameterTaint(Object s, Object[] args) {
		boolean tainted = ((Taintable) s).isTainted();

		for (Object arg : args) {
			if (arg instanceof Taintable) {
				tainted = tainted || ((Taintable) arg).isTainted();
			}
			if (tainted) break;
		}

		return tainted;
	}

	public static void addTaintToMethod(Object s, Object ret) {
		taint(s);
		if (ret != null) taint(ret);
	}

	public static void assertNonTaint(Object s, Object[] args, String signature) {
		checkTaint(s, signature);
		for (Object arg : args) checkTaint(arg, signature);
	}

	public static void detaintMethodReturn(Object ret) {
		detaint(ret);
	}
}
