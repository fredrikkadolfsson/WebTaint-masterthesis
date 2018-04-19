package se.adolfsson.dtp;

import org.junit.Test;
import se.adolfsson.dtp.utils.api.TaintTools;

import static se.adolfsson.dtp.TestUtils.assertTaintAndLog;


public class StringTests {

	@Test
	public void TaintPropagationStringConcat() {
		System.out.println("##### TAINT PROPAGATION CONCAT - " + String.class.getName());

		String tainted = "Tainted String";
		TaintTools.taint(tainted);
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

		TaintTools.detaint(tainted);
		TaintTools.detaint(notTainted);

		System.out.println();
	}
}
