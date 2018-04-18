package se.adolfsson.dtp.agent;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
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

    if (className.equals("se.adolfsson.dtp.IntTests")) {
      return addTaintVariables(className);
    }

    /*
    byte[] ret;
    if ((ret = sourceTransformer.isSource(className)) != null) return ret;
      //else if ((ret = isSink(className)) != null) return ret;
    else return null;
    */
    return null;
  }

  private byte[] addTaintVariables(String className) {
    System.out.println(className);

    ClassPool cp = ClassPool.getDefault();
    CtClass cClass = cp.getOrNull(className);

    if (cClass == null) {
      System.out.println("Class dose not exist");
      return null;
    }

    cClass.defrost();

    CtField[] fields = cClass.getFields();
    for (CtField field : fields) {
      String name = field.getName();
      System.out.println(name);
    }



    CtMethod[] cMethods = cClass.getDeclaredMethods();
      for (CtMethod cMethod : cMethods) {
      cMethod.
    }



    System.out.println();
    System.out.println();
    return null;
  }
}