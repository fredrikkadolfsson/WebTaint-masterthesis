package se.adolfsson.dtp.agent;

import se.adolfsson.dtp.utils.SourceTransformer;
import se.adolfsson.dtp.utils.SourcesOrSinks;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

public class TransformerAgent implements ClassFileTransformer {
  private SourceTransformer sourceTransformer;
  private SourcesOrSinks sinks;

  TransformerAgent(SourcesOrSinks sources, SourcesOrSinks sinks) {
    this.sourceTransformer = new SourceTransformer(sources, false);
    this.sinks = sinks;
  }

  @Override
  public byte[] transform(ClassLoader loader, String className,
                          Class classBeingRedefined, ProtectionDomain protectionDomain,
                          byte[] classfileBuffer) {

    className = className.replaceAll("/", ".");

    byte[] ret;
    if ((ret = sourceTransformer.isSource(className)) != null) return ret;
      //else if ((ret = isSink(className)) != null) return ret;
    else return null;
  }
}