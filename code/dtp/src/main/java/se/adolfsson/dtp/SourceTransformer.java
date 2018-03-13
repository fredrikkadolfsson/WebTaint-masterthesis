package se.adolfsson.dtp;

import javassist.ClassPool;
import lombok.extern.java.Log;

import java.security.ProtectionDomain;
import java.util.logging.Level;

@Log
class SourceTransformer {
  static boolean isSource(String className) {
    return className.equals("org/springframework/stereotype/Controller");
  }

  public byte[] transform(ClassLoader loader, String className,
                          Class classBeingRedefined, ProtectionDomain protectionDomain,
                          byte[] classfileBuffer) {

    log.log(Level.INFO, "########################################");
    log.log(Level.INFO, "");
    log.log(Level.INFO, "Transform " + className);
    ClassPool cp = ClassPool.getDefault();
    log.log(Level.INFO, "");
    log.log(Level.INFO, "########################################");

    return null;
  }
}