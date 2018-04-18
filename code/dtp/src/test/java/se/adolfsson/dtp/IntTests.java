package se.adolfsson.dtp;

import org.junit.Test;
import se.adolfsson.dtp.utils.api.TaintTools;

import static se.adolfsson.dtp.TestUtils.assertTaintAndLog;

public class IntTests {
  int a = 1;
  int b = 1;

  @Test
  public void TaintPropagationStringConcat() {
    System.out.println(a);
    System.out.println("##### TAINT PROPAGATION - " + int.class.getName());

    int tainted = 10;
    TaintTools.taint(tainted);
    int notTainted = 0;

    assertTaintAndLog(tainted, true);
    assertTaintAndLog(notTainted, false);

    int tmp = tainted + 1;
    assertTaintAndLog(tmp, true);

    assertTaintAndLog(notTainted + notTainted, false);
    assertTaintAndLog(tainted + notTainted, true);
    assertTaintAndLog(notTainted + notTainted, false);
    assertTaintAndLog(tainted + tainted, true);

    TaintTools.detaint(tainted);
    TaintTools.detaint(notTainted);

    System.out.println();
  }
}