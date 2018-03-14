package se.adolfsson.dtp;

import java.io.IOException;
import java.lang.instrument.Instrumentation;

import static se.adolfsson.dtp.SourceAndSinkReference.getSinks;
import static se.adolfsson.dtp.SourceAndSinkReference.getSources;

public class TaintAgent {
  public static void premain(String agentArgs, Instrumentation inst) {
    System.out.println("Executing taint premain.........");
    System.out.println();
    try {
      inst.addTransformer(new TransformerAgent(getSources(), getSinks()));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}