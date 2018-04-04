package se.adolfsson.dtp.utils;

import javassist.*;
import lombok.extern.java.Log;
import se.adolfsson.dtp.utils.api.Taintable;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;

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

      SourceAndSinkReference source = sources.stream().filter(src -> src.getClazz().equals(className)).findFirst().get();
      String[] methods = source.getMethods();

      for (String method : methods) {
        print("\t" + method);

        try {
          CtMethod cMethod = cClass.getDeclaredMethod(method);
          CtClass[] cParams = cMethod.getParameterTypes();

          for (int i = 0; i < cParams.length; i++) {
            print("\t\t " + cParams[i].getName());
            CtClass[] interfaces = cParams[i].getInterfaces();

            for (CtClass interfac : interfaces) {
              if (interfac.getName().equals(Taintable.class.getName())) {
                print("\t\t\t is Taintable");

                if (cMethod.isEmpty()) print("\t\t\t EMPTY"); //TODO FIX HANDLING OF EMPTY METHOD BODY
                else cMethod.insertBefore("{ $" + (i + 1) + ".setTaint(true); }");
                break;
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
}
