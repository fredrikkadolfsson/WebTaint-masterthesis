package se.adolfsson.dtp.utils;

import se.adolfsson.dtp.utils.api.TaintException;
import se.adolfsson.dtp.utils.api.Taintable;

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

	public static void propagateMethodTaint(Object s, Object ret) {
		if (s instanceof Taintable) {
			((Taintable) s).setTaint(true);
		}
		((Taintable) ret).setTaint(true);
	}

	public static void checkMethodTaint(Object s, Object[] args, String methodName) {
		if (s instanceof Taintable && ((Taintable) s).isTainted()) {
			throw new TaintException("TAINTED LOVE!!!", methodName);
		}

		for (Object arg : args) {
			if (arg instanceof Taintable && ((Taintable) arg).isTainted()) {
				throw new TaintException("TAINTED LOVE!!!", methodName);
			}
		}
	}
}
