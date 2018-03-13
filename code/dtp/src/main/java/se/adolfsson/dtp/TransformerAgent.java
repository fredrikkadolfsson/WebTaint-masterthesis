package se.adolfsson.dtp;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

import static se.adolfsson.dtp.SourceTransformer.isSource;

public class TransformerAgent implements ClassFileTransformer {

  @Override
  public byte[] transform(ClassLoader loader, String className,
                          Class classBeingRedefined, ProtectionDomain protectionDomain,
                          byte[] classfileBuffer) {

    if (isSource(className))
      return new SourceTransformer().transform(loader, className, classBeingRedefined, protectionDomain, classfileBuffer);

    return null;
  }
}