package se.adolfsson.dtp.utils;

import javassist.CtMethod;
import javassist.Modifier;
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

  public static boolean isNative(CtMethod method) {
    return Modifier.isNative(method.getModifiers());
  }

  public static boolean isStatic(CtMethod method) {
    return Modifier.isStatic(method.getModifiers());
  }
}
