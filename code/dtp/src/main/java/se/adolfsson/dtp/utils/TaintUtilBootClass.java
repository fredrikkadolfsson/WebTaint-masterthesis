package se.adolfsson.dtp.utils;

import se.adolfsson.dtp.utils.api.Taintable;

public class TaintUtilBootClass {
  static public boolean propagateParameterTaint(Object s, Object[] args) {
    boolean tainted = ((Taintable) s).isTainted();
    for (Object arg : args) {
      if (arg instanceof Taintable) {
        tainted = tainted || ((Taintable) arg).isTainted();
      }

      if (tainted) break;
    }

    return tainted;
  }
}
