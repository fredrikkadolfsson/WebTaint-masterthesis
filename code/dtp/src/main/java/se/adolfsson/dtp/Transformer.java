package se.adolfsson.dtp;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

//this class will be registered with instrumentation agent
public class Transformer implements ClassFileTransformer {
  public byte[] transform(ClassLoader loader, String className,
                          Class classBeingRedefined, ProtectionDomain protectionDomain,
                          byte[] classfileBuffer) {

    //System.out.println("Instrumenting: " + className);

    return null;
  }
}