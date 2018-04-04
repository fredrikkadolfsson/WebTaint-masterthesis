package se.adolfsson.dtp.utils;

import javassist.*;
import lombok.extern.java.Log;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;

import static se.adolfsson.dtp.utils.TaintUtils.isNative;
import static se.adolfsson.dtp.utils.TaintUtils.isStatic;

@Log
public class SourceTransformer {
  private List<SourceOrSink> sources;
  private Boolean isAgent;

  public SourceTransformer(List<SourceOrSink> sources, Boolean isAgent) {
    this.sources = sources;
    this.isAgent = isAgent;
  }

  public boolean isSource(String className) {
    for (SourceOrSink source : sources) {
      if (className.equals(source.getClazz())) return true;
    }

    return false;
  }

  public byte[] transform(String className) {
    ClassPool cp = ClassPool.getDefault();
    return transformSources(cp, className);
  }

  public byte[] transformSources(ClassPool cp, String className) {
    try {
      print("########################################");
      print("");
      print("Transforming: " + className);
      print("Methods: ");

      CtClass cClass = cp.get(className);

      if (cClass.isInterface()) {
        print("\tIS INTERFACE!!!");

      } else {
        cClass.defrost();

        SourceOrSink source = sources.stream().filter(src -> src.getClazz().equals(className)).findFirst().get();
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
                  cMethod.insertAfter("{ if ($_ != null) $_.setTaint(true);  }");
                }
              }
            }
          } catch (NotFoundException e) {
            print("\t\tdose not exist");
          }
        }
      }

      print("");
      print("########################################");

      if (cClass.isInterface()) return null;
      else return cClass.toBytecode();

    } catch (NotFoundException | IOException | CannotCompileException e) {
      e.printStackTrace();
      return null;
    }
  }

  private void print(String content) {
    if (isAgent) log.log(Level.INFO, content);
    else System.out.println(content);
  }
}
