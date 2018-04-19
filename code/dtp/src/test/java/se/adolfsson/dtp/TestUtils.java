package se.adolfsson.dtp;

import se.adolfsson.dtp.utils.api.TaintTools;

import static junit.framework.TestCase.assertEquals;

class TestUtils {
  static void assertTaintAndLog(Object s, boolean taintExpected) {
    final boolean tainted = TaintTools.isTainted(s);

    assertEquals(taintExpected, tainted);

    System.out.println(
        (tainted == taintExpected ? " OK" : "NOK") +
            " | expected " + (taintExpected ? "" : "not ") + "tainted, taint = " + tainted);
  }
}
