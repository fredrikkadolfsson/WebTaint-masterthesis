package se.adolfsson.dtp;

import org.junit.Test;
import se.adolfsson.dtp.utils.api.TaintUtil;

import static se.adolfsson.dtp.TestUtils.assertTaintAndLog;


public class StringBufferTests {

  @Test
  public void TaintPropagationStringBufferAppend() {
    System.out.println("##### TAINT PROPAGATION APPEND - StringBuffer");

    StringBuffer tainted = new StringBuffer("StringBuffer");
    TaintUtil.taint(tainted);
    StringBuffer notTainted = new StringBuffer("StringBuffer");

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