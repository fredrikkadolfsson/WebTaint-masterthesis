import java.lang.instrument.Instrumentation;

public class TaintAgent {
  public static void premain(String agentArgs, Instrumentation inst) {
    System.out.println("Executing taint premain.........");
    inst.addTransformer(new StringTransformer());
  }
}