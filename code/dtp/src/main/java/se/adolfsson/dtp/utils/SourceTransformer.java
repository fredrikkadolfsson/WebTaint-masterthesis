package se.adolfsson.dtp.utils;

import javassist.*;
import lombok.extern.java.Log;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;

import static se.adolfsson.dtp.utils.SourcesOrSinks.isSourceOrSink;


@Log
public class SourceTransformer {
  private SourcesOrSinks sources;
  private Boolean isAgent;

  public SourceTransformer(SourcesOrSinks sources, Boolean isAgent) {
    this.sources = sources;
    this.isAgent = isAgent;
  }

  private static boolean isNative(CtMethod method) {
    return Modifier.isNative(method.getModifiers());
  }

  private static boolean isStatic(CtMethod method) {
    return Modifier.isStatic(method.getModifiers());
  }

  public byte[] isSource(String className) {
    try {
      ClassPool cp = ClassPool.getDefault();
      if (cp.get(className).isInterface()) return null;
    } catch (NotFoundException ignored) {
      return null;
    }

    String ret;
    if (isSourceClass(className)) return transform(className, className, true);
    else if ((ret = usesInterface(className)) != null) return transform(className, ret, false);
    else return extendsSourceClassOrNull(className);
  }

  private byte[] extendsSourceClassOrNull(String className) {
    ClassPool cp = ClassPool.getDefault();

    try {
      CtClass cClass = cp.get(className);
      CtClass scClass = cClass.getSuperclass();
      return isSource(scClass.getName());
    } catch (NotFoundException ignored) {
    }

    return null;
  }

  private byte[] transform(String className, String alteredAsClassName, boolean isClass) {
    ClassPool cp = ClassPool.getDefault();
    try {
      print("########################################");
      print("");
      print("Transforming: " + className + (className.equals(alteredAsClassName) ? "" : " as Interface " + alteredAsClassName));
      print("Methods: ");

      CtClass cClass = cp.get(className);
      cClass.defrost();

      List<SourceOrSink> sources = (isClass ? this.sources.getClasses() : this.sources.getInterfaces());

      SourceOrSink source = sources.stream()
          .filter(
              src -> src.getClazz().equals(alteredAsClassName)
          ).findFirst().get();

      String[] methods = source.getMethods();

      for (String method : methods) {
        print("\t" + method);

        try {
          CtMethod[] cMethods = cClass.getDeclaredMethods(method);

          for (CtMethod cMethod : cMethods) {
            String returnType = cMethod.getReturnType().getName();
            if (!isStatic(cMethod) &&
                !isNative(cMethod)) {

              if (returnType.equals(String.class.getName()) ||
                  returnType.equals(StringBuilder.class.getName()) ||
                  returnType.equals(StringBuffer.class.getName())) {
                print("\t\tSource Defined");
                cMethod.insertAfter("{ if ($_ != null) $_.setTaint(true);  }");
              }
            }
          }
        } catch (NotFoundException e) {
          print("\t\tdose not exist");
        }
      }

      print("");
      print("########################################");

      return cClass.toBytecode();

    } catch (NotFoundException | IOException | CannotCompileException e) {
      e.printStackTrace();
      return null;
    }
  }

  private void print(String content) {
    if (isAgent) log.log(Level.INFO, content);
    else System.out.println(content);
  }

  private boolean isSourceClass(String className) {
    return isSourceOrSink(sources.getClasses(), className);
  }

  private String usesInterface(String className) {
    boolean ret;
    for (SourceOrSink interfazz : sources.getInterfaces()) {
      ret = implementsSourceInterface(interfazz.getClazz(), className);
      if (ret) return interfazz.getClazz();
    }

    return extendsSourceInterface(className);
  }

  private String extendsSourceInterface(String clazz) {
    boolean ret;
    for (SourceOrSink interfazz : sources.getInterfaces()) {
      ClassPool cp = ClassPool.getDefault();

      try {
        CtClass cClass = cp.get(clazz);
        CtClass ecClass = cp.get(interfazz.getClazz());

        ret = cClass.subtypeOf(ecClass);

        if (ret) return interfazz.getClazz();
      } catch (NotFoundException ignored) {
      }
    }
    return null;
  }

  private boolean implementsSourceInterface(String interfazz, String className) {
    ClassPool cp = ClassPool.getDefault();

    try {
      CtClass cClass = cp.get(className);

      return cClass.subtypeOf(cp.get(interfazz));
    } catch (NotFoundException ignored) {
    }

    return false;
  }
}
