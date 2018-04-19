package se.adolfsson.dtp;

import org.junit.Test;
import se.adolfsson.dtp.utils.api.TaintTools;

import static se.adolfsson.dtp.TestUtils.assertTaintAndLog;


public class StringBufferTests {

	@Test
	public void TaintPropagationStringBufferAppend() {
		System.out.println("##### TAINT PROPAGATION APPEND - " + StringBuffer.class.getName());

		StringBuffer tainted = new StringBuffer("StringBuffer");
		TaintTools.taint(tainted);
		StringBuffer notTainted = new StringBuffer("StringBuffer");

		assertTaintAndLog(tainted, true);
		assertTaintAndLog(notTainted, false);

		assertTaintAndLog(notTainted.append(notTainted), false);
		assertTaintAndLog(tainted.append(notTainted), true);
		assertTaintAndLog(notTainted.append(notTainted), false);
		assertTaintAndLog(notTainted.append(tainted), true);
		assertTaintAndLog(notTainted, true);
		assertTaintAndLog(notTainted.toString(), true);

		TaintTools.detaint(tainted);
		TaintTools.detaint(notTainted);

		System.out.println();
	}
}
