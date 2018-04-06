package se.adolfsson.dtp.utils;

import javassist.*;
import lombok.extern.java.Log;
import se.adolfsson.dtp.utils.api.Taintable;

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
    if (isSourceClass(className)) return transform(className, className);
    else if ((ret = usesInterface(className)) != null) return transform(className, ret);
    else if ((ret = extendsSourceClass(className)) != null) return transform(className, ret);
    else return null;
  }

  private String extendsSourceClass(String className) {
    ClassPool cp = ClassPool.getDefault();

    try {
      CtClass cClass = cp.get(className);
      CtClass scClass = cClass.getSuperclass();

      if (scClass != null) return isSuperSource(scClass.getName());
    } catch (NotFoundException ignored) {
    }

    return null;
  }

  private String isSuperSource(String className) {
    String ret;
    if (isSourceClass(className)) return className;
    else if ((ret = usesInterface(className)) != null) return ret;
    else if ((ret = extendsSourceClass(className)) != null) return ret;
    else return null;
  }

  public byte[] transform(String className, String alteredAsClassName) {
    ClassPool cp = ClassPool.getDefault();
    try {
      print("########################################");
      print("");
      print("Transforming: " + className + (className.equals(alteredAsClassName) ? "" : " as " + alteredAsClassName));

      CtClass cClass = cp.getOrNull(className);
      if (cClass == null) {
        print("\tClass not loaded");
        print("");
        print("########################################");
        return null;
      }

      print("Methods: ");
      cClass.defrost();

      List<SourceOrSink> sources = (cp.get(alteredAsClassName).isInterface() ? this.sources.getInterfaces() : this.sources.getClasses());

      SourceOrSink source = sources.stream()
          .filter(
              src -> src.getClazz().equals(alteredAsClassName)
          ).findFirst().get();

      String[] methods = source.getMethods();

      for (String method : methods) {
        print("\t" + method);

        CtMethod[] cMethods = cClass.getDeclaredMethods(method);

        if (cMethods.length > 0) {
          for (CtMethod cMethod : cMethods) {
            if (!isStatic(cMethod) &&
                !isNative(cMethod)) {
              CtClass returnType = cMethod.getReturnType();
              if (returnType.subtypeOf(ClassPool.getDefault().get(Taintable.class.getName()))) {
                cMethod.insertAfter("{ if ($_ != null) $_.setTaint(true);  }");
                print("\t\tSource Defined");
              } else print("\t\t Untaintable returntype: " + returnType.getName());
            } else print("\t\tStatic or Native Method, can't taint");
          }
        } else print("\t\tDo not exist in class");
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
