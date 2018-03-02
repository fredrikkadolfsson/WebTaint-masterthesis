package se.adolfsson.pcm;

import javassist.*;

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

    CtField taintField = new CtField(CtClass.booleanType, "tainted", cClass);
    taintField.setModifiers(Modifier.PRIVATE);
    cClass.addField(taintField, "false");

    cClass.addMethod(CtMethod.make("public void setTaint(boolean value){ this.tainted = value; }", cClass));
    cClass.addMethod(CtMethod.make("public boolean isTainted(){ return this.tainted; }", cClass));

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
