package se.adolfsson.dtp.agent;

import javassist.CannotCompileException;
import javassist.CtClass;

import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

import static se.adolfsson.dtp.utils.SourcesSinksOrSanitizers.*;

public class TransformerAgent implements ClassFileTransformer {

	@Override
	public byte[] transform(ClassLoader loader, String className,
	                        Class classBeingRedefined, ProtectionDomain protectionDomain,
	                        byte[] classfileBuffer) {

		className = className.replaceAll("/", ".");

		try {
			CtClass ret, tmp;
			ret = isSourceSinkOrSanitizer(getSources(), className, null);
			if ((tmp = isSourceSinkOrSanitizer(getSinks(), className, ret)) != null) ret = tmp;
			if ((tmp = isSourceSinkOrSanitizer(getSanitizers(), className, ret)) != null) ret = tmp;
			if (ret != null) return ret.toBytecode();
		} catch (IOException | CannotCompileException e) {
			e.printStackTrace();
		}
		return null;
	}
}