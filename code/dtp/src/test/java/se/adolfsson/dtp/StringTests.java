package se.adolfsson.dtp;

import org.junit.Test;
import se.adolfsson.dtp.pcm.api.TaintUtil;

import static se.adolfsson.dtp.TestUtils.assertTaintAndLog;


public class StringTests {

  @Test
  public void TaintPropagationStringConcat() {
    System.out.println("##### TAINT PROPAGATION CONCAT - String");

    String tainted = "Tainted String";
    TaintUtil.taint(tainted);
    String notTainted = "Not Tainted String";

    assertTaintAndLog(tainted, true);
    assertTaintAndLog(notTainted, false);

    int taintedCharsLength = tainted.length();
    int charsLength = notTainted.length();

    char[] arrc = notTainted.toCharArray();
    assertTaintAndLog(arrc, false);

    tainted.getChars(0, taintedCharsLength, arrc, charsLength - taintedCharsLength);

    assertTaintAndLog(arrc, true);
    assertTaintAndLog(new String(arrc), true);

    /*
    assertTaintAndLog(notTainted + notTainted, false);
    assertTaintAndLog(tainted + notTainted, true);
    assertTaintAndLog(notTainted + notTainted, false);
    assertTaintAndLog(tainted + tainted, true);
    */
    System.out.println();
  }
}
