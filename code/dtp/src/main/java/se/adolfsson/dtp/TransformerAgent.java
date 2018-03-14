package se.adolfsson.dtp;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;
import java.util.List;

public class TransformerAgent implements ClassFileTransformer {
  private List<SourceAndSinkReference> sources;
  private List<SourceAndSinkReference> sinks;

  TransformerAgent(List<SourceAndSinkReference> sources, List<SourceAndSinkReference> sinks) {
    this.sources = sources;
    this.sinks = sinks;
  }

  @Override
  public byte[] transform(ClassLoader loader, String className,
                          Class classBeingRedefined, ProtectionDomain protectionDomain,
                          byte[] classfileBuffer) {

    SourceTransformer sourceTransformer = new SourceTransformer(sources);

    if (sourceTransformer.isSource(className))
      return sourceTransformer.transform(loader, className, classBeingRedefined, protectionDomain, classfileBuffer);

    return null;
  }
}