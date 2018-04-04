package se.adolfsson.dtp;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import se.adolfsson.dtp.utils.api.TaintTools;

import java.util.Arrays;
import java.util.Collection;

import static se.adolfsson.dtp.TestUtils.assertTaintAndLog;

@RunWith(Parameterized.class)
public class MutualTests {
  private Object fObject;

  public MutualTests(Object object) {
    fObject = object;
  }

  @Parameterized.Parameters(name = "{0}")
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][]{
        {"String"},
        {new StringBuilder("StringBuilder")},
        {new StringBuffer("StringBuffer")}
    });
  }

  @Test
  public void TaintAndDetaint() {
    System.out.println("##### TAINT AND DETAINT - " + fObject);

    assertTaintAndLog(fObject, false);
    TaintTools.taint(fObject);
    assertTaintAndLog(fObject, true);
    TaintTools.detaint(fObject);
    assertTaintAndLog(fObject, false);

    System.out.println();
  }

  @Test
  public void TaintPropagation() {
    System.out.println("##### TAINT PROPAGATION - " + fObject);

    TaintTools.taint(fObject);
    assertTaintAndLog(fObject, true);
    Object objectCopy = fObject;
    assertTaintAndLog(objectCopy, true);

    System.out.println();
  }
}
