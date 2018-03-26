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
      cClass.defrost();

      SourceAndSinkReference source = sources.stream().filter(src -> src.getClazz().equals(className)).findFirst().get();
      String[] methods = source.getMethods();

      for (String method : methods) {
        log.log(Level.INFO, "\t" + method);

        CtMethod cMethod = cClass.getDeclaredMethod(method);
        CtClass[] cParams = cMethod.getParameterTypes();

        for (int i = 0; i < cParams.length; i++) {
          log.log(Level.INFO, "\t\t " + cParams[i].getName());
          CtClass[] interfaces = cParams[i].getInterfaces();

          for (CtClass interfac : interfaces) {
            if (interfac.getName().equals(Taintable.class.getName())) {
              log.log(Level.INFO, "\t\t\t is Taintable");

              if (cMethod.isEmpty()) log.log(Level.INFO, "\t\t\t EMPTY"); //TODO FIX HANDLING OF EMPTY METHOD BODY
              else cMethod.insertBefore("{ $" + (i + 1) + ".setTaint(true); }");
              break;
            }
          }
        }

        /*
        CtClass retClass = cMethod.getReturnType();
        if (retClass instanceof Taintable) {
          cMethod.insertAfter("{ $_.setTaint(true); }");
          cMethod.insertAfter("System.out.println( $_.tainted );");  // remove later, just for testing
          log.log(Level.INFO, "\t\t return");
        }
        */
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
