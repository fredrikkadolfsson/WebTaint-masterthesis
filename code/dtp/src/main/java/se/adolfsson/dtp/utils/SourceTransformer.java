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

    String ret;
    if (isClass(className)) return transform(className, className, true);
    else if ((ret = isInterface(className)) != null) return transform(className, ret, false);

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

  private boolean isClass(String className) {
    return isSourceOrSink(sources.getClasses(), className);
  }

  private String isInterface(String className) {
    ClassPool cp = ClassPool.getDefault();

    try {
      CtClass cClass = cp.get(className);

      for (SourceOrSink source : sources.getInterfaces()) {
        boolean ret = cClass.subtypeOf(cp.get(source.getClazz()));

        if (ret) return source.getClazz();
      }
    } catch (NotFoundException ignored) {
    }

    return null;
  }
}
