package se.adolfsson.dtp;

import javassist.*;
import lombok.extern.java.Log;
import se.adolfsson.dtp.pcm.api.Taintable;

import java.io.IOException;
import java.security.ProtectionDomain;
import java.util.List;
import java.util.logging.Level;

@Log
class SourceTransformer {
  private List<SourceAndSinkReference> sources;

  SourceTransformer(List<SourceAndSinkReference> sources) {
    this.sources = sources;
  }

  boolean isSource(String className) {
    for (SourceAndSinkReference source : sources) {
      if (className.equals(source.getClazz())) return true;
    }

    return false;
  }

  public byte[] transform(ClassLoader loader, String className,
                          Class classBeingRedefined, ProtectionDomain protectionDomain,
                          byte[] classfileBuffer) {
    try {
      log.log(Level.INFO, "########################################");
      log.log(Level.INFO, "");
      log.log(Level.INFO, "Transforming: " + className);
      log.log(Level.INFO, "Methods: ");

      ClassPool cp = ClassPool.getDefault();
      CtClass cClass = cp.get(className);

      SourceAndSinkReference source = sources.stream().filter(src -> src.getClazz().equals(className)).findFirst().get();
      String[] methods = source.getMethods();

      for (String method : methods) {
        log.log(Level.INFO, "\t" + method);

        CtMethod cMethod = cClass.getDeclaredMethod(method);

        CtClass[] cParams = cMethod.getParameterTypes();
        System.out.println(cParams.length);
        for (int i = 1; i < cParams.length + 1; i++) {
          log.log(Level.INFO, "\t\t param?" + i);

          if (cParams[i] instanceof Taintable) {
            log.log(Level.INFO, "\t\t parampre" + i);

            cMethod.insertBefore("{ $" + i + ".setTaint(true); }");
            log.log(Level.INFO, "\t\t param" + i);
          }
          log.log(Level.INFO, "\t\t paramnote" + i);

        }

        CtClass retClass = cMethod.getReturnType();
        System.out.println(retClass);
        if (retClass instanceof Taintable) {
          cMethod.insertAfter("{ $_.setTaint(true); }");
          cMethod.insertAfter("System.out.println( $_.tainted );");  // remove later, just for testing
          log.log(Level.INFO, "\t\t return");
        }
      }

      log.log(Level.INFO, "");
      log.log(Level.INFO, "########################################");

      return cClass.toBytecode();
    } catch (NotFoundException | IOException | CannotCompileException e) {
      e.printStackTrace();
      return null;
    }
  }
}
