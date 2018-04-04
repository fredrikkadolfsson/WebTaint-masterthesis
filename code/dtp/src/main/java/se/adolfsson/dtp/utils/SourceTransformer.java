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
  private List<SourceAndSinkReference> sources;
  private Boolean isAgent;

  public SourceTransformer(List<SourceAndSinkReference> sources, Boolean isAgent) {
    this.sources = sources;
    this.isAgent = isAgent;
  }

  public boolean isSource(String className) {
    for (SourceAndSinkReference source : sources) {
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
      cClass.defrost();

      if (cClass.isInterface()) {
        print("IS INTERFACE!!!");
        return null;

      } else {
        SourceAndSinkReference source = sources.stream().filter(src -> src.getClazz().equals(className)).findFirst().get();
        String[] methods = source.getMethods();

        for (String method : methods) {
          print("\t" + method);

          try {
            CtMethod cMethod = cClass.getDeclaredMethod(method);
            String returnType = cMethod.getReturnType().getName();

            if (!isStatic(cMethod) &&
                !isNative(cMethod)) {
              if (returnType.equals(String.class.getName()) ||
                  returnType.equals(StringBuilder.class.getName()) ||
                  returnType.equals(StringBuffer.class.getName())) {
                cMethod.insertAfter("{ $_.setTaint(true); }");
              }
            }

          } catch (NotFoundException e) {
            print("\t\tdose not exist");
          }
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
}
