package se.adolfsson.dtp;

import se.adolfsson.dtp.utils.api.TaintUtil;

import static org.junit.Assert.assertEquals;

class TestUtils {
  static void assertTaintAndLog(Object s, boolean taintExpected) {
    final boolean tainted = TaintUtil.isTainted(s);

    assertEquals(taintExpected, tainted);

    System.out.println(
        (tainted == taintExpected ? " OK" : "NOK") +
            " | expected " + (taintExpected ? "" : "not ") + "tainted, taint = " + tainted);
  }
}
