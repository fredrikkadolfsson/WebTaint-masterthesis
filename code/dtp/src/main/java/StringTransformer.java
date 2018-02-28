import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

//this class will be registered with instrumentation agent
public class StringTransformer implements ClassFileTransformer {
  public byte[] transform(ClassLoader loader, String className,
                          Class classBeingRedefined, ProtectionDomain protectionDomain,
                          byte[] classfileBuffer) {
    byte[] byteCode = classfileBuffer;

    System.out.println("Instrumenting......");

    return byteCode;
  }
}