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

    assertTaintAndLog(notTainted + notTainted, false);
    assertTaintAndLog(tainted + notTainted, true);
    assertTaintAndLog(notTainted + notTainted, false);
    assertTaintAndLog(tainted + tainted, true);

    System.out.println();

    assertTaintAndLog(tainted, true);
    assertTaintAndLog(notTainted, false);

    assertTaintAndLog(notTainted.concat(notTainted), false);
    assertTaintAndLog(tainted.concat(notTainted), true);
    assertTaintAndLog(notTainted.concat(notTainted), false);
    assertTaintAndLog(tainted.concat(tainted), true);

    System.out.println();
  }
}
