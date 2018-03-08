package se.adolfsson.dtp.pcm;

import javassist.*;
import se.adolfsson.dtp.pcm.api.Taintable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * We need to prepare a modification of java.lang.Stringuilder ahead of time that we can put on the bootclasspath,
 * since we are unable to add fields to any class that the java agent itself depends on (we can't get to them before
 * they are loaded the first time).
 */
public class TaintFieldAdder {

  public static void main(String[] args) {
    System.out.println();
    System.out.println("Staring TaintFieldAdder");

    new TaintFieldAdder().run();
    System.out.println();
  }

  public static boolean isNative(CtMethod method) {
    return Modifier.isNative(method.getModifiers());
  }

  public static boolean isStatic(CtMethod method) {
    return Modifier.isStatic(method.getModifiers());
  }

  private void run() {
    try {
      ClassPool cp = ClassPool.getDefault();

      addTaintFieldToClass(cp, String.class.getName());
      addTaintFieldToClass(cp, StringBuffer.class.getName());
      addTaintFieldToClass(cp, StringBuilder.class.getName());

    } catch (NotFoundException | CannotCompileException | IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void addTaintFieldToClass(ClassPool cp, String className) throws NotFoundException, CannotCompileException, IOException {
    CtClass cClass = cp.get(className);
    cClass.defrost();

    cClass.addInterface(cp.get(Taintable.class.getName()));

    CtField taintField = new CtField(CtClass.booleanType, "tainted", cClass);
    taintField.setModifiers(Modifier.PRIVATE);
    cClass.addField(taintField, "propagateParamTaint($args)");

    cClass.addMethod(CtMethod.make("public void setTaint(boolean value){ this.tainted = value; }", cClass));
    cClass.addMethod(CtMethod.make("public boolean isTainted(){ return this.tainted; }", cClass));
    cClass.addMethod(CtMethod.make("private boolean propagateParamTaint(Object[] args) {" +
        "    boolean tainted = false;" +
        "    for (int i = 0; i < args.length; i++) {" +
        "      if (args[i] instanceof " + Taintable.class.getName() + ") {" +
        "        tainted = tainted || ((" + Taintable.class.getName() + ") args[i]).isTainted();" +
        "      }" +
        "    }" +
        "    return tainted;" +
        "  }", cClass));

    CtMethod[] cMethods = cClass.getDeclaredMethods();
    for (CtMethod cMethod : cMethods) {
      if (cMethod.getParameterTypes().length > 0 &&
          !isNative(cMethod) &&
          !isStatic(cMethod) &&
          !cMethod.getName().equals("setTaint") &&
          !cMethod.getName().equals("isTainted") &&
          !cMethod.getName().equals("propagateParamTaint")) {
        cMethod.insertBefore("{ $0.setTaint($0.propagateParamTaint($args)); }");
      }
    }

    byte[] bytes = cClass.toBytecode();

    System.out.println("Added taint to: " + className + " " + bytes.length);

    final String s = className.replace(".", "/");
    File f = new File("build/taint/" + s + ".class");
    f.getParentFile().mkdirs();
    final FileOutputStream fos = new FileOutputStream(f);

    fos.write(bytes);
    fos.flush();
    fos.close();
  }
}
