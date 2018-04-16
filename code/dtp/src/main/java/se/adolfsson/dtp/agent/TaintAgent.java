package se.adolfsson.dtp.agent;

import java.lang.instrument.Instrumentation;

public class TaintAgent {
  public static void premain(String agentArgs, Instrumentation inst) {
    System.out.println("Executing taint premain.........");
    System.out.println();
    /*
    try {
      inst.addTransformer(new TransformerAgent(getSources(), getSinks()));
    } catch (IOException e) {
      e.printStackTrace();
    }
    */
  }
}