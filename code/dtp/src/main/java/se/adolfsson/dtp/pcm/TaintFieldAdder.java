package se.adolfsson.dtp.pcm;

import javassist.*;
import se.adolfsson.dtp.pcm.api.TaintUtilBootClass;
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

  private static boolean isNative(CtMethod method) {
    return Modifier.isNative(method.getModifiers());
  }

  private static boolean isStatic(CtMethod method) {
    return Modifier.isStatic(method.getModifiers());
  }

  private void run() {
    try {
      ClassPool cp = ClassPool.getDefault();
      cp.importPackage(TaintUtilBootClass.class.getName());

      addTaintableToClass(cp, String.class.getName());
      addTaintableToClass(cp, StringBuffer.class.getName());
      addTaintableToClass(cp, StringBuilder.class.getName());
      writeClass(cp, Taintable.class.getName());
      writeClass(cp, TaintUtilBootClass.class.getName());

    } catch (NotFoundException | CannotCompileException e) {
      throw new RuntimeException(e);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void addTaintableToClass(ClassPool cp, String className) throws NotFoundException, CannotCompileException, IOException {
    CtClass cClass = cp.get(className);
    cClass.defrost();

    cClass.addInterface(cp.get(Taintable.class.getName()));

    addTaintVar(cClass);
    addTaintMethods(cClass);
    propagateTaintInMethods(cClass);
    writeClass(cp, className);
  }

  private void addTaintVar(CtClass cClass) throws CannotCompileException {
    CtField taintField = new CtField(CtClass.booleanType, "tainted", cClass);
    taintField.setModifiers(Modifier.PRIVATE);
    cClass.addField(taintField, "TaintUtilBootClass.propagateParameterTaint($0, $args)");
  }

  private void addTaintMethods(CtClass cClass) throws CannotCompileException {
    cClass.addMethod(CtMethod.make("public void setTaint(boolean value){ this.tainted = value; }", cClass));
    cClass.addMethod(CtMethod.make("public boolean isTainted(){ return this.tainted; }", cClass));
  }

  private void propagateTaintInMethods(CtClass cClass) throws NotFoundException, CannotCompileException {
    CtMethod[] cMethods = cClass.getDeclaredMethods();
    for (CtMethod cMethod : cMethods) {
      if (cMethod.getParameterTypes().length > 0 &&
          !isNative(cMethod) &&
          !isStatic(cMethod) &&
          !cMethod.getName().equals("setTaint") &&
          !cMethod.getName().equals("isTainted")) {
        cMethod.insertBefore("{ $0.setTaint(TaintUtilBootClass.propagateParameterTaint($0, $args)); }");
      }
    }
  }

  private void writeClass(ClassPool cp, String className) throws IOException, CannotCompileException, NotFoundException {
    CtClass cClass = cp.get(className);
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
