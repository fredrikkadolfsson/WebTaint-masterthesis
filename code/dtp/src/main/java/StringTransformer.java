import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

//this class will be registered with instrumentation agent
public class StringTransformer implements ClassFileTransformer {
  public byte[] transform(ClassLoader loader, String className,
                          Class classBeingRedefined, ProtectionDomain protectionDomain,
                          byte[] classfileBuffer) {
    byte[] byteCode = classfileBuffer;

    System.out.println("Instrumenting......");


        /*
    try {
      ClassPool classPool = ClassPool.getDefault();
      CtClass ctClass = classPool.makeClass(new ByteArrayInputStream(
          classfileBuffer));
      CtMethod[] methods = ctClass.getDeclaredMethods();
      for (CtMethod method : methods) {
        method.addLocalVariable("startTime", CtClass.longType);
        method.insertBefore("startTime = System.nanoTime();");
        method.insertAfter("System.out.println(\"Execution Duration "
            + "(nano sec): \"+ (System.nanoTime() - startTime) );");
      }
      byteCode = ctClass.toBytecode();
      ctClass.detach();
      System.out.println("Instrumentation complete.");
    } catch (Throwable ex) {
      System.out.println("Exception: " + ex);
      ex.printStackTrace();
    }
    */
    return byteCode;
  }
}