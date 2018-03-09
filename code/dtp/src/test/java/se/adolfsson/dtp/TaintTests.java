package se.adolfsson.dtp;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import se.adolfsson.dtp.pcm.api.TaintUtil;

import java.util.Arrays;
import java.util.Collection;

import static se.adolfsson.dtp.TestUtils.assertTaintAndLog;

@RunWith(Parameterized.class)
public class TaintTests {

  private Object fObject;

  public TaintTests(Object object) {
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
    TaintUtil.taint(fObject);
    assertTaintAndLog(fObject, true);
    TaintUtil.detaint(fObject);
    assertTaintAndLog(fObject, false);

    System.out.println();
  }

  @Test
  public void TaintPropagation() {
    System.out.println("##### TAINT PROPAGATION - " + fObject);

    TaintUtil.taint(fObject);
    assertTaintAndLog(fObject, true);
    Object objectCopy = fObject;
    assertTaintAndLog(objectCopy, true);

    System.out.println();
  }
}
