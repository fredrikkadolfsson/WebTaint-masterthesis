package se.adolfsson.dtp;

import org.junit.Test;
import se.adolfsson.dtp.utils.api.TaintTools;

import static se.adolfsson.dtp.TestUtils.assertTaintAndLog;


public class StringBuilderTests {

  @Test
  public void TaintPropagationStringBufferAppend() {
    System.out.println("##### TAINT PROPAGATION APPEND - StringBuilder");

    StringBuffer tainted = new StringBuffer("StringBuilder");
    TaintTools.taint(tainted);
    StringBuffer notTainted = new StringBuffer("StringBuilder");

    assertTaintAndLog(tainted, true);
    assertTaintAndLog(notTainted, false);

    assertTaintAndLog(notTainted.append(notTainted), false);
    assertTaintAndLog(tainted.append(notTainted), true);
    assertTaintAndLog(notTainted.append(notTainted), false);
    assertTaintAndLog(notTainted.append(tainted), true);
    assertTaintAndLog(notTainted, true);
    assertTaintAndLog(notTainted.toString(), true);

    System.out.println();
  }
}
