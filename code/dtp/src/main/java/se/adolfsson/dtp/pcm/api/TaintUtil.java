package se.adolfsson.dtp.pcm.api;


import lombok.extern.java.Log;

import java.lang.reflect.Field;
import java.util.logging.Level;

@Log
public class TaintUtil {
  static public void taint(Object s) {
    setTaint(s, true);
  }

  static public void detaint(Object s) {
    setTaint(s, false);
  }

  static private void setTaint(Object s, boolean value) {
    if (!(s instanceof Taintable)) {
      log.log(Level.INFO, "Attempted to set taint on " + s.getClass().getName() + " to " + value + ", but not Taintable");
      return;
    }
    ((Taintable) s).setTaint(value);
  }

  static private Field taintField(Object s) throws NoSuchFieldException {
    return s.getClass().getField("tainted");
  }

  static public boolean isTainted(Object s) {
    if (!(s instanceof Taintable)) {
      log.log(Level.INFO, "Attempted to query taint on " + s.getClass().getName() + ", but not Taintable");
      return false;
    }
    return ((Taintable) s).isTainted();
  }

  static public void checkTaint(Object s, String signature) {
    if (isTainted(s))
      throw new TaintException(s.toString(), signature);
  }
}
