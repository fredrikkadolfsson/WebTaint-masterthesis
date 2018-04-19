package se.adolfsson.dtp.utils;

import se.adolfsson.dtp.utils.api.Taintable;

import static se.adolfsson.dtp.utils.api.TaintTools.checkTaint;

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

		if (ret != null) ((Taintable) ret).setTaint(true);
	}

	public static void checkMethodTaint(Object s, Object[] args, String signature) {
		checkTaint(s, signature);
		for (Object arg : args) checkTaint(arg, signature);
	}
}
