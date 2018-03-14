package se.adolfsson.dtp;

import javassist.*;
import lombok.extern.java.Log;

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
        CtMethod cMethod = cClass.getDeclaredMethod(method);

        //cMethod.setBody("{ $0.setTaint(true); }");

        log.log(Level.INFO, "\t" + method);
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
