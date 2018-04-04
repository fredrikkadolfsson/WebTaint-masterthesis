package se.adolfsson.dtp.agent;

import se.adolfsson.dtp.utils.SourceAndSinkReference;
import se.adolfsson.dtp.utils.SourceTransformer;

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

    SourceTransformer sourceTransformer = new SourceTransformer(sources, true);

    className = className.replaceAll("/", ".");

    if (sourceTransformer.isSource(className))
      return sourceTransformer.transform(className);

    return null;
  }
}